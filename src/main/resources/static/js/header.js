/**
 * Скрипт для демо-хедера:
 * - Загружает список пользователей через API
 * - Управляет выбором пользователя
 * - Синхронизирует состояние с localStorage
 * - Генерирует событие userChanged
 */

(function() {
    // Дожидаемся полной загрузки DOM
    document.addEventListener("DOMContentLoaded", async () => {
        // Элементы DOM
        const userSelect = document.getElementById("demoUser");
        const manualUserIdInput = document.getElementById("manualUserId");
        const setUserIdBtn = document.getElementById("setUserIdBtn");

        if (!userSelect) {
            // На страницах без хедера выходим
            return;
        }

        // Функция загрузки списка пользователей
        async function loadUsersList() {
            try {
                // Запрос к админскому API с явным указанием роли ADMIN
                const response = await window.GamificationAPI.apiRequest("/api/v1/admin/users?page=0&size=1000");
                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
                }
                const pageData = await response.json();
                const users = pageData.content || [];

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

        function onUserSelectChange() {
            const newUserId = userSelect.value;
            if (!newUserId) return;
            window.GamificationAPI.setCurrentUserId(newUserId);
            if (manualUserIdInput) manualUserIdInput.value = newUserId;
            document.dispatchEvent(new Event("userChanged"));
        }

        // Ручной ввод userId
        function setManualUserId() {
            const newUserId = manualUserIdInput.value.trim();
            if (!newUserId) {
                console.warn("[Header] Пустой userId");
                return;
            }
            window.GamificationAPI.setCurrentUserId(newUserId);
            // Синхронизируем select
            let optionExists = false;
            for (let i = 0; i < userSelect.options.length; i++) {
                if (userSelect.options[i].value === newUserId) {
                    userSelect.selectedIndex = i;
                    optionExists = true;
                    break;
                }
            }
            if (!optionExists) {
                const option = document.createElement("option");
                option.value = newUserId;
                option.textContent = `${newUserId} (вручную)`;
                option.selected = true;
                userSelect.appendChild(option);
            }
            document.dispatchEvent(new Event("userChanged"));
        }

        // Инициализация значений из localStorage
        const savedUserId = window.GamificationAPI.getCurrentUserId();
        if (manualUserIdInput) manualUserIdInput.value = savedUserId;

        // Загружаем список пользователей и навешиваем обработчики
        await loadUsersList();

        // Обработчики событий
        userSelect.addEventListener("change", onUserSelectChange);
        if (setUserIdBtn) setUserIdBtn.addEventListener("click", setManualUserId);
        if (manualUserIdInput) {
            manualUserIdInput.addEventListener("keypress", (e) => {
                if (e.key === "Enter") setManualUserId();
            });
        }

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

    window.goToLeaderboard = function() {
        // Открываем страницу лидерборда с возможностью выбора курса/группы
        window.location.href = '/demo/leaderboard';
    };
})();