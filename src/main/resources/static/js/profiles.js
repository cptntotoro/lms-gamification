(function() {
    const profilesContent = document.getElementById('profilesContent');
    const loadingDiv = document.getElementById('profilesLoading');

    async function loadUsersWithCourses() {
        try {
            const role = window.GamificationAPI.getCurrentRole();
            const response = await window.GamificationAPI.apiRequest('/api/admin/users/with-courses', {
                method: 'GET',
                role: role
            });
            if (!response.ok) {
                throw new Error(`Ошибка HTTP ${response.status}`);
            }
            const users = await response.json();
            renderProfiles(users);
        } catch (error) {
            console.error('Ошибка загрузки профилей:', error);
            loadingDiv.innerHTML = `<div class="error-message">Не удалось загрузить данные: ${error.message}</div>`;
        }
    }

    function renderProfiles(users) {
        if (!users || users.length === 0) {
            profilesContent.innerHTML = '<div class="empty-message">Нет пользователей или данных о зачислениях</div>';
            profilesContent.style.display = 'block';
            loadingDiv.style.display = 'none';
            return;
        }

        let html = '';
        for (const user of users) {
            const enrollments = user.enrollments || [];
            html += `
                <div class="user-card">
                    <div class="user-header">
                        <span>👤</span>
                        <span class="user-id">${escapeHtml(user.userId)}</span>
                        <span style="font-size:0.85rem; font-weight:normal;">(${enrollments.length} ${declension(enrollments.length, 'курс', 'курса', 'курсов')})</span>
                    </div>
                    <div style="overflow-x: auto;">
                        <table class="enrollments-table">
                            <thead>
                                <tr><th>Курс (ID)</th><th>Группа</th><th>Очки в курсе</th><th>Действие</th></tr>
                            </thead>
                            <tbody>
            `;
            if (enrollments.length === 0) {
                html += `<tr><td colspan="4" class="empty-message">Пользователь не зачислен ни на один курс</td></tr>`;
            } else {
                for (const enr of enrollments) {
                    const courseId = enr.courseId || '—';
                    const groupId = enr.groupId || '—';
                    const points = enr.pointsInCourse !== undefined ? enr.pointsInCourse : '—';
                    html += `
                        <tr>
                            <td><strong>${escapeHtml(courseId)}</strong></td>
                            <td><span class="badge-group">${escapeHtml(groupId)}</span></td>
                            <td>${points}</td>
                            <td class="actions-cell">
                                <a class="event-link course-link" data-user-id="${escapeHtml(user.userId)}" data-course-id="${escapeHtml(courseId)}">📊 Лидерборд курса</a>
                                <a class="event-link course-link" data-user-id="${escapeHtml(user.userId)}" data-course-id="${escapeHtml(courseId)}" data-group-id="${escapeHtml(groupId)}">📊 Лидерборд группы</a>
                                <a class="event-link profile-link" data-user-id="${escapeHtml(user.userId)}">📊 Профиль</a>
                                <a class="event-link profile-link" data-user-id="${escapeHtml(user.userId)}" data-course-id="${escapeHtml(courseId)}">📊 Профиль с курсом</a>
                                <a class="event-link profile-link" data-user-id="${escapeHtml(user.userId)}" data-course-id="${escapeHtml(courseId)}" data-group-id="${escapeHtml(groupId)}">📊 Профиль с курсом и группой</a>
                            </td>
                        </tr>
                    `;
                }
            }
            html += `</tbody></table></div></div>`;
        }
        profilesContent.innerHTML = html;
        profilesContent.style.display = 'block';
        loadingDiv.style.display = 'none';

        // Навешиваем обработчики на ссылки
        document.querySelectorAll('.course-link').forEach(link => {
            const userId = link.getAttribute('data-user-id');
            const courseId = link.getAttribute('data-course-id');
            const groupId = link.getAttribute('data-group-id');
            let url = `/demo/leaderboard/course/${encodeURIComponent(courseId)}/user/${encodeURIComponent(userId)}`;
            if (groupId && groupId !== '—') {
                url += `?groupId=${encodeURIComponent(groupId)}`;
            }
            link.href = url;
        });

        // Навешиваем обработчики на ссылки
        document.querySelectorAll('.profile-link').forEach(link => {
            const userId = link.getAttribute('data-user-id');
            const courseId = link.getAttribute('data-course-id');
            const groupId = link.getAttribute('data-group-id');
            let url = `/demo/users/${encodeURIComponent(userId)}`;
            if (courseId && courseId !== '—') {
                url += `?courseId=${encodeURIComponent(courseId)}`;
            }
            if (groupId && groupId !== '—') {
                url += courseId && courseId !== '—' ? '&' : '?';
                url += `groupId=${encodeURIComponent(groupId)}`;
            }
            link.href = url;
        });
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/[&<>]/g, function(m) {
            if (m === '&') return '&amp;';
            if (m === '<') return '&lt;';
            if (m === '>') return '&gt;';
            return m;
        });
    }

    function declension(n, one, two, five) {
        n = Math.abs(n) % 100;
        if (n >= 11 && n <= 19) return five;
        const last = n % 10;
        if (last === 1) return one;
        if (last >= 2 && last <= 4) return two;
        return five;
    }

    // Загрузка при старте
    loadUsersWithCourses();

    // Обновление при смене пользователя/роли (опционально)
    document.addEventListener('userChanged', () => {
        profilesContent.style.display = 'none';
        loadingDiv.style.display = 'block';
        loadingDiv.innerHTML = 'Загрузка...';
        loadUsersWithCourses();
    });
})();