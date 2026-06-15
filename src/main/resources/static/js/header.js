/**
 * Скрипт для демо-хедера:
 * - Загружает список пользователей через общий API
 * - Управляет выбором пользователя
 * - Синхронизирует состояние с localStorage
 * - Генерирует событие userChanged
 * - Слушает событие usersChanged для обновления списка
 */

(function() {
    // Дожидаемся полной загрузки DOM
    document.addEventListener("DOMContentLoaded", async () => {
        // Элементы DOM
        const userSelect = document.getElementById("demoUser");

        if (!userSelect) {
            // На страницах без хедера выходим
            return;
        }

        // Функция загрузки и отображения списка пользователей (использует общий fetchUsersList)
        async function loadAndRenderUsers() {
            try {
                const users = await window.GamificationAPI.fetchUsersList();
                const currentUserId = window.GamificationAPI.getCurrentUserId();

                // Очищаем и заполняем select
                userSelect.innerHTML = "";
                users.forEach(user => {
                    const option = document.createElement("option");
                    option.value = user.userId;
                    option.textContent = `${user.userId} (${user.totalPoints} XP, lvl ${user.level})`;
                    if (user.userId === currentUserId) {
                        option.selected = true;
                    }
                    userSelect.appendChild(option);
                });

                // Если текущий пользователь не найден в списке – добавляем опцию с ним
                if (currentUserId && !users.some(u => u.userId === currentUserId)) {
                    const option = document.createElement("option");
                    option.value = currentUserId;
                    option.textContent = `${currentUserId} (вне списка)`;
                    option.selected = true;
                    userSelect.appendChild(option);
                }

                console.log(`[Header] Загружено пользователей: ${users.length}`);
            } catch (error) {
                console.error("[Header] Ошибка загрузки списка пользователей:", error);
                userSelect.innerHTML = '<option value="">-- ошибка загрузки --</option>';
            }
        }

        function onUserSelectChange(userId) {
            const newUserId = userId || userSelect.value;
            userSelect.value = newUserId;
            if (!newUserId) return;
            window.GamificationAPI.setCurrentUserId(newUserId);
            document.dispatchEvent(new Event("userChanged"));
        }

        // Загружаем список пользователей и навешиваем обработчики
        await loadAndRenderUsers();

        // Обработчики событий
        userSelect.addEventListener("change", () => onUserSelectChange());

        // Слушаем событие обновления списка пользователей (например, после создания нового пользователя)
        document.addEventListener('usersChanged', async (e) => {
            await loadAndRenderUsers();
            onUserSelectChange(e.detail.userId)
        });

        // После первичной загрузки диспатчим событие, чтобы виджет/панель обновились
        document.dispatchEvent(new Event("userChanged"));
    });

    // Глобальные функции навигации для всех страниц
    window.goToProfile = function() {
        const userId = window.GamificationAPI.getCurrentUserId();
        if (userId) {
            window.location.href = `/demo/admin/users/${encodeURIComponent(userId)}`;
        } else {
            console.warn('Не удалось определить пользователя для перехода в профиль');
        }
    };
})();