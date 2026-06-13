/**
 * Скрипт для страницы лидерборда:
 * - Извлекает userId, courseId, groupId из URL (если есть)
 * - Синхронизирует выбор пользователя в хедере
 * - Загружает курсы для пользователя
 * - Предвыбирает курс/группу и автоматически загружает лидерборд
 * - Реагирует на смену пользователя в хедере
 */

(function() {
    let currentUserId = null;               // ID текущего пользователя
    let coursesList = [];                   // список всех курсов {courseId, enrolled, totalPointsInCourse}
    let groupsList = [];                    // список групп для выбранного курса {groupId, member}
    let urlCourseId = null;                 // ID курса из URL
    let urlGroupId = null;                  // ID группы из URL
    let urlUserId = null;                   // ID пользователя из URL

    // DOM-элементы
    const userDisplaySpan = document.getElementById('currentUserIdDisplay');
    const courseSelect = document.getElementById('leaderboardCourseSelect');
    const groupSelect = document.getElementById('leaderboardGroupSelect');
    const loadBtn = document.getElementById('loadLeaderboardBtn');
    const container = document.getElementById('leaderboardContainer');

    // Вспомогательная функция: извлечь параметры из текущего URL
    function parseUrlParams() {
        // Шаблон пути: /demo/leaderboard/course/{courseId}/user/{userId}
        const pathParts = window.location.pathname.split('/');
        let courseId = null;
        let userId = null;
        for (let i = 0; i < pathParts.length; i++) {
            if (pathParts[i] === 'course' && i + 1 < pathParts.length) {
                courseId = decodeURIComponent(pathParts[i + 1]);
            }
            if (pathParts[i] === 'user' && i + 1 < pathParts.length) {
                userId = decodeURIComponent(pathParts[i + 1]);
            }
        }
        const searchParams = new URLSearchParams(window.location.search);
        const groupId = searchParams.get('groupId') || null;
        return { courseId, userId, groupId };
    }

    // Установить пользователя в хедере (localStorage и select), если нужно
    function syncHeaderUser(userId) {
        if (!userId) return;
        const currentHeaderUser = window.GamificationAPI.getCurrentUserId();
        if (currentHeaderUser !== userId) {
            window.GamificationAPI.setCurrentUserId(userId);
            // Генерируем событие, чтобы header.js обновил свой select
            document.dispatchEvent(new Event('userChanged'));
        }
    }

    // Обновить отображаемое имя пользователя в верхней полоске
    function updateUserDisplay(userId) {
        if (userDisplaySpan) userDisplaySpan.textContent = userId || '—';
    }

    // Загрузить ВСЕ курсы для заданного userId (с признаком enrolled)
    async function loadAllCourses(userId) {
        if (!userId) return [];
        try {
            const response = await window.GamificationAPI.apiRequest(`/demo/leaderboard/courses/all?userId=${encodeURIComponent(userId)}`, {
                method: 'GET'
            });
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            const data = await response.json();
            return data; // массив CourseWithEnrollmentDto
        } catch (error) {
            console.error('Ошибка загрузки курсов:', error);
            return [];
        }
    }

    // Загрузить группы для курса с членством пользователя
    async function loadGroupsForCourse(courseId, userId) {
        if (!courseId || !userId) return [];
        try {
            const response = await window.GamificationAPI.apiRequest(`/demo/leaderboard/courses/${encodeURIComponent(courseId)}/groups?userId=${encodeURIComponent(userId)}`, {
                method: 'GET'
            });
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            const data = await response.json();
            return data; // массив GroupWithMembershipDto
        } catch (error) {
            console.error('Ошибка загрузки групп:', error);
            return [];
        }
    }

    // Заполнить select курсов, отметить записанные, добавить опцию "Добавить новый"
    function populateCourseSelect(courses, preselectedCourseId) {
        courseSelect.innerHTML = '<option value="">-- Выберите курс --</option>';
        if (courses.length === 0) {
            courseSelect.innerHTML = '<option value="">-- Нет доступных курсов --</option>';
            return false;
        }
        courses.forEach(course => {
            const option = document.createElement('option');
            option.value = course.courseId;
            let text = `${course.courseId}${course.displayName ? ' (' + course.displayName + ')' : ''}`;
            if (course.enrolled) {
                text += ` ✓ (${course.totalPointsInCourse || 0} очков)`;
            } else {
                text += ` (не записан)`;
            }
            option.textContent = text;
            if (course.enrolled) {
                option.style.fontWeight = 'bold';
                option.style.backgroundColor = '#e6f7e6';
            }
            courseSelect.appendChild(option);
        });
        // Добавляем опцию "➕ Добавить новый курс"
        const addOption = document.createElement('option');
        addOption.value = '__ADD_NEW_COURSE__';
        addOption.textContent = '➕ Добавить новый курс';
        addOption.style.fontStyle = 'italic';
        addOption.style.color = '#2563eb';
        courseSelect.appendChild(addOption);

        if (preselectedCourseId && courses.some(c => c.courseId === preselectedCourseId)) {
            courseSelect.value = preselectedCourseId;
            return true;
        } else if (courses.length > 0 && courses.some(c => c.enrolled)) {
            // предвыбираем первый записанный курс, если есть
            const firstEnrolled = courses.find(c => c.enrolled);
            if (firstEnrolled) {
                courseSelect.value = firstEnrolled.courseId;
                return true;
            }
        }
        return false;
    }

    // Заполнить select групп, отметить членство, добавить опцию "Добавить новую"
    function populateGroupSelect(groups, preselectedGroupId) {
        groupSelect.innerHTML = '<option value="">-- Все группы --</option>';
        if (groups.length === 0) {
            groupSelect.innerHTML = '<option value="">-- Нет групп --</option>';
        } else {
            groups.forEach(group => {
                const option = document.createElement('option');
                option.value = group.groupId;
                let text = `${group.groupId}${group.displayName ? ' (' + group.displayName + ')' : ''}`;
                if (group.member) {
                    text += ` ✓`;
                    option.style.fontWeight = 'bold';
                    option.style.backgroundColor = '#e6f7e6';
                }
                option.textContent = text;
                groupSelect.appendChild(option);
            });
        }
        // Добавляем опцию "➕ Добавить новую группу"
        const addOption = document.createElement('option');
        addOption.value = '__ADD_NEW_GROUP__';
        addOption.textContent = '➕ Добавить новую группу';
        addOption.style.fontStyle = 'italic';
        addOption.style.color = '#2563eb';
        groupSelect.appendChild(addOption);

        if (preselectedGroupId && groups.some(g => g.groupId === preselectedGroupId)) {
            groupSelect.value = preselectedGroupId;
            return true;
        }
        return false;
    }

    // Обработчик выбора курса
    async function onCourseChange() {
        const selectedCourseId = courseSelect.value;
        if (!selectedCourseId || selectedCourseId === '__ADD_NEW_COURSE__') {
            if (selectedCourseId === '__ADD_NEW_COURSE__') {
                const newCourseId = prompt('Введите идентификатор нового курса:');
                if (newCourseId && newCourseId.trim()) {
                    // Добавляем новый курс в селектор (временно, без проверки существования)
                    const newOption = document.createElement('option');
                    newOption.value = newCourseId.trim();
                    newOption.textContent = `${newCourseId.trim()} (новый)`;
                    newOption.style.fontWeight = 'bold';
                    newOption.style.backgroundColor = '#ffffcc';
                    // Вставляем перед опцией добавления
                    const addOption = courseSelect.querySelector('option[value="__ADD_NEW_COURSE__"]');
                    courseSelect.insertBefore(newOption, addOption);
                    courseSelect.value = newCourseId.trim();
                    // Загружаем группы для нового курса (скорее всего пусто)
                    await loadAndPopulateGroups(newCourseId.trim());
                } else {
                    // восстанавливаем предыдущее значение, если было
                    if (coursesList.length > 0) {
                        const firstEnrolled = coursesList.find(c => c.enrolled);
                        courseSelect.value = firstEnrolled ? firstEnrolled.courseId : coursesList[0].courseId;
                    } else {
                        courseSelect.value = '';
                    }
                }
            }
            return;
        }
        // Загружаем группы для выбранного курса
        await loadAndPopulateGroups(selectedCourseId);
    }

    async function loadAndPopulateGroups(courseId) {
        if (!currentUserId) return;
        groupsList = await loadGroupsForCourse(courseId, currentUserId);
        populateGroupSelect(groupsList, urlGroupId);
    }

    // Загрузить лидерборд на основе выбранных в данный момент курса и группы
    async function loadLeaderboard() {
        const courseId = courseSelect.value;
        if (!courseId || courseId === '__ADD_NEW_COURSE__') {
            container.innerHTML = '<div class="loading-overlay">Пожалуйста, выберите курс</div>';
            return;
        }
        let groupId = groupSelect.value;
        if (groupId === '__ADD_NEW_GROUP__') {
            const newGroupId = prompt('Введите идентификатор новой группы:');
            if (newGroupId && newGroupId.trim()) {
                groupId = newGroupId.trim();
                // Добавляем новую группу в селектор временно
                const newOption = document.createElement('option');
                newOption.value = groupId;
                newOption.textContent = `${groupId} (новая)`;
                newOption.style.fontWeight = 'bold';
                newOption.style.backgroundColor = '#ffffcc';
                const addOption = groupSelect.querySelector('option[value="__ADD_NEW_GROUP__"]');
                groupSelect.insertBefore(newOption, addOption);
                groupSelect.value = groupId;
            } else {
                groupId = '';
                groupSelect.value = '';
            }
        }
        const userId = currentUserId;

        if (!userId) {
            container.innerHTML = '<div class="error-message">Пользователь не определён</div>';
            return;
        }

        container.innerHTML = '<div class="loading-overlay"><div class="loading-small" style="margin: 0 auto;"></div> Загрузка лидерборда...</div>';

        try {
            let url = `/api/v1/leaderboard/course/${encodeURIComponent(courseId)}/user/${encodeURIComponent(userId)}?page=0&size=50`;
            if (groupId && groupId !== '__ADD_NEW_GROUP__') url += `&groupId=${encodeURIComponent(groupId)}`;

            const response = await window.GamificationAPI.apiRequest(url, {
                method: 'GET',
                overrideHeaders: { 'X-User-Id': userId }
            });
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            const leaderboardData = await response.json();
            renderLeaderboard(leaderboardData, courseId, groupId);
        } catch (error) {
            console.error('Ошибка загрузки лидерборда:', error);
            container.innerHTML = `<div class="error-message">Ошибка загрузки лидерборда: ${error.message}</div>`;
        }
    }

    // Отрисовать лидерборд
    function renderLeaderboard(data, courseId, groupId) {
        const topEntries = data.topEntries || [];
        const currentUserRank = data.currentUserRank;
        const currentUserPoints = data.currentUserPoints;

        if (!topEntries.length && !currentUserRank) {
            container.innerHTML = `<div class="empty-state"><div class="empty-icon">🏆</div><p class="empty-text">Пока никто не набрал очков на этом курсе</p></div>`;
            return;
        }

        let html = `
            <div class="course-meta">
                <div class="meta-item">
                    <span class="meta-label">Курс</span>
                    <span class="meta-value">${escapeHtml(courseId)}</span>
                </div>
                <div class="meta-item">
                    <span class="meta-label">Группа</span>
                    <span class="meta-value">${groupId ? escapeHtml(groupId) : 'Все группы'}</span>
                </div>
                ${currentUserRank ? `<div class="meta-item"><span class="meta-label">Ваше место</span><span class="meta-value">${currentUserRank}</span></div>` : ''}
                ${currentUserPoints !== undefined && currentUserPoints !== null ? `<div class="meta-item"><span class="meta-label">Ваши очки</span><span class="meta-value">${currentUserPoints}</span></div>` : ''}
            </div>
            <div class="leaderboard-wrapper card">
                <div class="table-responsive">
                    <table class="leaderboard-table">
                        <thead>
                            <tr><th class="col-rank text-center">Место</th><th class="col-participant text-center">Участник</th><th class="col-points text-center">Очки</th></tr>
                        </thead>
                        <tbody>
        `;

        topEntries.forEach((entry, idx) => {
            const rank = entry.rank || '—';
            const userIdDisplay = entry.userId || '—';
            const points = entry.pointsInCourse || 0;
            const isCurrent = entry.isCurrentUser === true;
            const medalClass = idx === 0 ? 'medal-gold' : (idx === 1 ? 'medal-silver' : (idx === 2 ? 'medal-bronze' : ''));
            const medalSymbol = idx === 0 ? '🥇' : (idx === 1 ? '🥈' : (idx === 2 ? '🥉' : rank));

            html += `
                <tr class="${isCurrent ? 'is-current-user' : ''}">
                    <td class="cell-rank text-center">
                        <span class="${medalClass}">${medalSymbol}</span>
                    </td>
                    <td class="cell-participant">
                        <div class="participant">
                            <div class="participant-avatar">${escapeHtml(userIdDisplay.substring(0,1).toUpperCase())}</div>
                            <div class="participant-info">
                                <div class="participant-name">${escapeHtml(userIdDisplay)}${isCurrent ? ' <span class="medal-you-badge">Вы</span>' : ''}</div>
                                ${entry.globalLevel ? `<div class="participant-level">Уровень <strong>${entry.globalLevel}</strong></div>` : ''}
                            </div>
                        </div>
                    </td>
                    <td class="cell-points">${points}</td>
                </tr>
            `;
        });

        html += `</tbody></table></div></div>`;
        container.innerHTML = html;
    }

    // Экранирование HTML-символов для безопасности
    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/[&<>]/g, function(m) {
            if (m === '&') return '&amp;';
            if (m === '<') return '&lt;';
            if (m === '>') return '&gt;';
            return m;
        });
    }

    // Главная инициализация
    async function init() {
        // Разбор параметров URL
        const params = parseUrlParams();
        urlCourseId = params.courseId;
        urlUserId = params.userId;
        urlGroupId = params.groupId;

        // Определение эффективного пользователя: из URL, если есть, иначе из хедера
        let effectiveUserId = urlUserId || window.GamificationAPI.getCurrentUserId();
        if (!effectiveUserId) {
            effectiveUserId = 'student001'; // значение по умолчанию
        }

        // Если в URL указан другой пользователь, синхронизируем хедер
        if (urlUserId && urlUserId !== window.GamificationAPI.getCurrentUserId()) {
            syncHeaderUser(urlUserId);
        }
        currentUserId = effectiveUserId;
        updateUserDisplay(currentUserId);

        // Загрузка ВСЕХ курсов для этого пользователя
        coursesList = await loadAllCourses(currentUserId);
        // Заполнить select курсов, предвыбрав курс из URL, если есть
        const hasCourse = populateCourseSelect(coursesList, urlCourseId);
        // Если в URL была группа, подставить её в поле выбора (загрузка групп произойдёт после выбора курса)
        if (urlGroupId) {
            // Группы ещё не загружены, сохраним для последующей установки
        }

        // Если курс был предвыбран (из URL или первый доступный), загружаем группы и лидерборд
        if (hasCourse && courseSelect.value && courseSelect.value !== '__ADD_NEW_COURSE__') {
            await loadAndPopulateGroups(courseSelect.value);
            if (urlGroupId) {
                // пытаемся установить группу, если она есть в списке
                const groupOption = groupSelect.querySelector(`option[value="${escapeHtml(urlGroupId)}"]`);
                if (groupOption) groupSelect.value = urlGroupId;
                else groupSelect.value = '';
            }
            await loadLeaderboard();
        } else if (!hasCourse && coursesList.length === 0) {
            container.innerHTML = '<div class="empty-state">Нет доступных курсов</div>';
        }

        // Слушаем событие смены пользователя в хедере
        document.addEventListener('userChanged', async () => {
            const newUserId = window.GamificationAPI.getCurrentUserId();
            if (newUserId === currentUserId) return;
            currentUserId = newUserId;
            updateUserDisplay(currentUserId);
            // Перезагружаем все курсы для нового пользователя
            coursesList = await loadAllCourses(currentUserId);
            const hasAny = populateCourseSelect(coursesList, null);
            if (hasAny && courseSelect.value && courseSelect.value !== '__ADD_NEW_COURSE__') {
                await loadAndPopulateGroups(courseSelect.value);
                await loadLeaderboard();
            } else {
                container.innerHTML = '<div class="loading-overlay">Выберите курс и нажмите «Показать лидерборд»</div>';
            }
        });

        // Слушаем изменение курса
        courseSelect.addEventListener('change', async () => {
            await onCourseChange();
            if (courseSelect.value && courseSelect.value !== '__ADD_NEW_COURSE__') {
                await loadLeaderboard();
            } else {
                container.innerHTML = '<div class="loading-overlay">Выберите курс</div>';
            }
        });

        // Слушаем изменение группы
        groupSelect.addEventListener('change', async () => {
            if (courseSelect.value && courseSelect.value !== '__ADD_NEW_COURSE__') {
                await loadLeaderboard();
            }
        });
    }

    // Навешиваем обработчик на кнопку
    if (loadBtn) {
        loadBtn.addEventListener('click', loadLeaderboard);
    }

    // Стартуем, если API доступен
    if (window.GamificationAPI) {
        init();
    } else {
        console.error('GamificationAPI не загружен');
        container.innerHTML = '<div class="error-message">Ошибка инициализации API</div>';
    }
})();