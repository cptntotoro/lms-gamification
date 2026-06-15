(function() {
    // Получаем userId из модели (Thymeleaf) и параметров URL
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
    const groupSelect = document.getElementById('groupIdSelectProfile');  // изменён id
    const loadBtn = document.getElementById('loadCourseStatsBtn');
    const statsContainer = document.getElementById('courseStatsContainer');
    const statsGrid = document.getElementById('courseStatsGrid');
    const errorDiv = document.getElementById('courseStatsError');

    let coursesList = []; // { courseId, groupId, pointsInCourse }

    // Функция загрузки курсов пользователя (через админский API /demo/users)
    async function loadUserCourses() {
        try {
            const response = await window.GamificationAPI.apiRequest('/demo/users', {
                method: 'GET'
            });
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            const users = await response.json();
            const currentUser = users.find(u => u.userId === userId);
            if (!currentUser || !currentUser.enrollments || currentUser.enrollments.length === 0) {
                courseSelect.innerHTML = '<option value="">-- Нет курсов --</option>';
                groupSelect.innerHTML = '<option value="">-- Нет групп --</option>';
                return;
            }
            coursesList = currentUser.enrollments.map(enr => ({
                courseId: enr.courseId,
                groupId: enr.groupId,
                pointsInCourse: enr.pointsInCourse || 0,
                displayName: enr.displayName
            }));
            // Заполняем select курсами
            courseSelect.innerHTML = '<option value="">-- Выберите курс --</option>';
            coursesList.forEach(course => {
                const option = document.createElement('option');
                option.value = course.courseId;
                option.textContent = `${course.courseId}${course.displayName ? ' (' + course.displayName + ')' : ''} (${course.pointsInCourse} очков)`;
                option.className = 'option-enrolled'; // все курсы из списка — записанные
                courseSelect.appendChild(option);
            });

            // Восстанавливаем значения из URL параметров, если они есть
            const urlParams = new URLSearchParams(window.location.search);
            const urlCourseId = urlParams.get('courseId');
            const urlGroupId = urlParams.get('groupId');
            if (urlCourseId && coursesList.some(c => c.courseId === urlCourseId)) {
                courseSelect.value = urlCourseId;
                // Загружаем группы для выбранного курса
                await loadGroupsForCourseSelect(courseSelect.value);
                if (urlGroupId) {
                    // Устанавливаем выбранную группу, если она есть в списке
                    const optionExists = Array.from(groupSelect.options).some(opt => opt.value === urlGroupId);
                    if (optionExists) groupSelect.value = urlGroupId;
                    else groupSelect.value = '';
                }
            } else {
                groupSelect.innerHTML = '<option value="">-- Сначала выберите курс --</option>';
            }

            // Если курс выбран, автоматически загружаем статистику
            if (courseSelect.value) {
                loadCourseStats();
            }
        } catch (error) {
            console.error('Ошибка загрузки курсов:', error);
            courseSelect.innerHTML = '<option value="">-- Ошибка загрузки --</option>';
            errorDiv.classList.remove('hide');
            errorDiv.textContent = 'Не удалось загрузить курсы пользователя';
        }
    }

    // Функция загрузки групп для выбранного курса (использует общий API)
    async function loadGroupsForCourseSelect(courseId) {
        if (!courseId || !userId) {
            groupSelect.innerHTML = '<option value="">-- Нет курса --</option>';
            return;
        }
        try {
            const groups = await window.GamificationAPI.loadGroupsForCourse(courseId, userId);
            groupSelect.innerHTML = '<option value="">-- Без группы --</option>';
            groups.forEach(group => {
                const option = document.createElement('option');
                option.value = group.groupId;
                option.textContent = `${group.groupId}${group.displayName ? ' (' + group.displayName + ')' : ''}`;
                if (group.member) {
                    option.textContent += ` (участник)`;
                    option.className = 'option-enrolled';
                } else {
                    option.textContent += ` (не участник)`;
                }
                groupSelect.appendChild(option);
            });
            if (groups.length === 0) {
                const noneOption = document.createElement('option');
                noneOption.value = '';
                noneOption.textContent = '-- Нет групп --';
                noneOption.disabled = true;
                groupSelect.appendChild(noneOption);
            }
        } catch (error) {
            console.error('Ошибка загрузки групп:', error);
            groupSelect.innerHTML = '<option value="">-- Ошибка загрузки групп --</option>';
        }
    }

    // Загрузка статистики по выбранному курсу/группе
    async function loadCourseStats() {
        const courseId = courseSelect.value;
        if (!courseId) {
            statsContainer.classList.add('hide');
            return;
        }
        const groupId = groupSelect.value === '' ? undefined : groupSelect.value;

        // Показываем лоадер
        statsContainer.classList.remove('hide');
        statsGrid.innerHTML = '<div class="loading-small" style="margin: 20px auto;"></div>';
        errorDiv.classList.add('hide');

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

            let html = `
                    <div class="course-stat-item">
                        <div class="course-stat-label">Очки в курсе</div>
                        <div class="course-stat-value">${points}</div>
                    </div>
                    <div class="course-stat-item">
                        <div class="course-stat-label">Место в курсе</div>
                        <div class="course-stat-value">${rankCourse}</div>
                    </div>
                `;
            if (rankGroup !== null && rankGroup !== undefined) {
                html += `
                        <div class="course-stat-item">
                            <div class="course-stat-label">Место в группе</div>
                            <div class="course-stat-value">${rankGroup}</div>
                        </div>
                    `;
            }
            if (groupId && data.groupId && data.groupId !== groupId) {
                html += `<div class="course-stat-item"><div class="course-stat-label">Группа</div><div class="course-stat-value">${data.groupId}</div></div>`;
            }
            statsGrid.innerHTML = html;
        } catch (error) {
            console.error('Ошибка загрузки статистики курса:', error);
            errorDiv.classList.remove('hide');
            errorDiv.textContent = `Ошибка: ${error.message}`;
            statsGrid.innerHTML = '';
        }
    }

    // Инициализация: загружаем курсы и навешиваем обработчики
    if (window.GamificationAPI) {
        loadUserCourses().then(() => {
            if (loadBtn) loadBtn.addEventListener('click', loadCourseStats);
            if (courseSelect) {
                courseSelect.addEventListener('change', async () => {
                    // При смене курса сбрасываем статистику, но не загружаем автоматически (по кнопке)
                    statsContainer.classList.add('hide');
                    if (courseSelect.value) {
                        await loadGroupsForCourseSelect(courseSelect.value);
                    } else {
                        groupSelect.innerHTML = '<option value="">-- Сначала выберите курс --</option>';
                    }
                });
            }
        });
    } else {
        console.error('GamificationAPI не загружен');
    }
})();