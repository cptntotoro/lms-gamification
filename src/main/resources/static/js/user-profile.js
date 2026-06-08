
(function() {
    // Получаем userId из модели (Thymeleaf) и параметры URL
    let userId = null;
    try {
        // Считываем из data-атрибута или из глобальной переменной, созданной Thymeleaf
        const userElement = document.querySelector('.profile-card');
        if (userElement) {
            const nameElem = userElement.querySelector('h1');
            if (nameElem) userId = nameElem.textContent.trim();
        }
    } catch(e) { console.warn(e); }

    // Если не определили из DOM, возможно модель передала через переменную (но на стороне клиента её нет)
    // Пробуем взять из URL path (последний сегмент)
    if (!userId) {
        const pathParts = window.location.pathname.split('/');
        const lastPart = pathParts[pathParts.length - 1];
        if (lastPart && lastPart !== 'users') userId = lastPart;
    }

    if (!userId) {
        console.error('Не удалось определить userId');
        return;
    }

    const courseSelect = document.getElementById('courseSelectProfile');
    const groupInput = document.getElementById('groupIdInputProfile');
    const loadBtn = document.getElementById('loadCourseStatsBtn');
    const statsContainer = document.getElementById('courseStatsContainer');
    const statsGrid = document.getElementById('courseStatsGrid');
    const errorDiv = document.getElementById('courseStatsError');

    let coursesList = []; // { courseId, groupId, pointsInCourse }

    // Функция загрузки курсов пользователя (через админский API /api/admin/users/with-courses)
    async function loadUserCourses() {
        try {
            const response = await window.GamificationAPI.apiRequest('/api/admin/users/with-courses', {
                method: 'GET'
            });
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            const users = await response.json();
            const currentUser = users.find(u => u.userId === userId);
            if (!currentUser || !currentUser.enrollments || currentUser.enrollments.length === 0) {
                courseSelect.innerHTML = '<option value="">-- Нет курсов --</option>';
                return;
            }
            coursesList = currentUser.enrollments.map(enr => ({
                courseId: enr.courseId,
                groupId: enr.groupId,
                pointsInCourse: enr.pointsInCourse || 0
            }));
            // Заполняем select курсами
            courseSelect.innerHTML = '<option value="">-- Выберите курс --</option>';
            coursesList.forEach(course => {
                const option = document.createElement('option');
                option.value = course.courseId;
                option.textContent = `${course.courseId} (${course.pointsInCourse} очков)`;
                if (course.groupId && course.groupId !== '—') {
                    option.textContent += ` • группа ${course.groupId}`;
                }
                courseSelect.appendChild(option);
            });

            // Восстанавливаем значения из URL параметров, если они есть
            const urlParams = new URLSearchParams(window.location.search);
            const urlCourseId = urlParams.get('courseId');
            const urlGroupId = urlParams.get('groupId');
            if (urlCourseId && coursesList.some(c => c.courseId === urlCourseId)) {
                courseSelect.value = urlCourseId;
            }
            if (urlGroupId) {
                groupInput.value = urlGroupId;
            }
            // Если курс выбран, автоматически загружаем статистику
            if (courseSelect.value) {
                loadCourseStats();
            }
        } catch (error) {
            console.error('Ошибка загрузки курсов:', error);
            courseSelect.innerHTML = '<option value="">-- Ошибка загрузки --</option>';
            errorDiv.style.display = 'block';
            errorDiv.textContent = 'Не удалось загрузить курсы пользователя';
        }
    }

    // Загрузка статистики по выбранному курсу/группе
    async function loadCourseStats() {
        const courseId = courseSelect.value;
        if (!courseId) {
            statsContainer.style.display = 'none';
            return;
        }
        const groupId = groupInput.value.trim() || undefined;

        // Показываем лоадер
        statsContainer.style.display = 'block';
        statsGrid.innerHTML = '<div class="loading-small" style="margin: 20px auto;"></div>';
        errorDiv.style.display = 'none';

        try {
            // Используем API /api/v1/users/{userId} с параметрами courseId и groupId
            let url = `/api/v1/users/${encodeURIComponent(userId)}?courseId=${encodeURIComponent(courseId)}`;
            if (groupId) url += `&groupId=${encodeURIComponent(groupId)}`;
            const response = await window.GamificationAPI.apiRequest(url, {
                method: 'GET',
                overrideHeaders: {
                    'X-User-Id': userId  // важно: подменяем заголовок на просматриваемого пользователя
                }
            });
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            const data = await response.json();

            // Формируем отображение статистики по курсу
            const points = data.pointsInCourse !== undefined ? data.pointsInCourse : '—';
            const rankCourse = data.rankInCourse !== undefined ? data.rankInCourse : '—';
            const rankGroup = data.rankInGroup !== undefined ? data.rankInGroup : null;
            const groupDisplay = groupId ? groupId : (data.groupId || '—');

            let html = `
                    <div class="course-stat-item">
                        <div class="course-stat-label">🎯 Очки в курсе</div>
                        <div class="course-stat-value">${points}</div>
                    </div>
                    <div class="course-stat-item">
                        <div class="course-stat-label">🏆 Место в курсе</div>
                        <div class="course-stat-value">${rankCourse}</div>
                    </div>
                `;
            if (rankGroup !== null && rankGroup !== undefined) {
                html += `
                        <div class="course-stat-item">
                            <div class="course-stat-label">👥 Место в группе</div>
                            <div class="course-stat-value">${rankGroup}</div>
                        </div>
                    `;
            }
            if (groupId && data.groupId && data.groupId !== groupId) {
                html += `<div class="course-stat-item"><div class="course-stat-label">⚠️ Группа</div><div class="course-stat-value">${data.groupId}</div></div>`;
            }
            statsGrid.innerHTML = html;
        } catch (error) {
            console.error('Ошибка загрузки статистики курса:', error);
            errorDiv.style.display = 'block';
            errorDiv.textContent = `Ошибка: ${error.message}`;
            statsGrid.innerHTML = '';
        }
    }

    // Инициализация: загружаем курсы и навешиваем обработчики
    if (window.GamificationAPI) {
        loadUserCourses().then(() => {
            if (loadBtn) loadBtn.addEventListener('click', loadCourseStats);
            if (courseSelect) courseSelect.addEventListener('change', () => {
                // При смене курса сбрасываем статистику, но не загружаем автоматически (по кнопке)
                statsContainer.style.display = 'none';
            });
        });
    } else {
        console.error('GamificationAPI не загружен');
    }
})();