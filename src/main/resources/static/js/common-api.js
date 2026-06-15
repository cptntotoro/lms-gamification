/**
 * Общий модуль для API-запросов
 * Предоставляет единую конфигурацию заголовков и работу с текущим пользователем/ролью.
 * Добавлены функции для работы с курсами, группами и списком пользователей.
 */

(function() {
    // Ключи localStorage
    const STORAGE_USER_ID = "demoUserId";

    // Значения по умолчанию
    const DEFAULT_USER_ID = "student001";

    // Токен для аутентификации (фиксированный для демо)
    const AUTH_TOKEN = "@%dCxrBAF7ozyF346l7$us0Vc31c5Z18";

    // --- Геттеры / сеттеры ---
    function getCurrentUserId() {
        return localStorage.getItem(STORAGE_USER_ID) || DEFAULT_USER_ID;
    }

    function setCurrentUserId(userId) {
        localStorage.setItem(STORAGE_USER_ID, userId);
    }

    /**
     * Выполнить API-запрос с автоматической подстановкой заголовков
     * @param {string} url - URL (абсолютный или относительный)
     * @param {Object} options - параметры fetch (method, body, headers и т.д.)
     * @param {Object} options.overrideHeaders - дополнительные заголовки (переопределяют стандартные)
     * @param {Object} options.role - роль от имени которой выполняется запрос
     * @returns {Promise<Response>}
     */
    async function apiRequest(url, options = {}) {
        const { overrideHeaders = {}, role = 'ADMIN', ...fetchOptions } = options;

        const headers = {
            "Content-Type": "application/json",
            "X-User-Id": getCurrentUserId(),
            "X-Role": role,
            "token": AUTH_TOKEN,
            ...overrideHeaders
        };

        const config = {
            ...fetchOptions,
            headers: headers
        };

        try {
            const response = await fetch(url, config);
            return response;
        } catch (error) {
            console.error("API request failed:", error);
            throw error;
        }
    }

    /**
     * Получить конфигурацию для виджета геймификации
     * @returns {Object} конфиг для initGamificationWidget
     */
    function getWidgetConfig() {
        return {
            apiBaseUrl: window.location.origin,
            userId: getCurrentUserId(),
            updateIntervalMs: 5000,
            headers: {
                "X-User-Id": getCurrentUserId(),
                "X-Role": 'STUDENT',
                "token": AUTH_TOKEN
            }
        };
    }

    /**
     * Получить список всех пользователей (ADMIN API)
     * @returns {Promise<Array>} массив объектов пользователей
     */
    async function fetchUsersList() {
        try {
            const response = await apiRequest("/api/v1/admin/users?page=0&size=1000");
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            const pageData = await response.json();
            return pageData.content || [];
        } catch (error) {
            console.error("Ошибка загрузки списка пользователей:", error);
            return [];
        }
    }

    /**
     * Загрузить все курсы с информацией о записи пользователя
     * @param {string} userId - ID пользователя
     * @returns {Promise<Array>} массив курсов {courseId, enrolled, totalPointsInCourse, ...}
     */
    async function loadAllCourses(userId) {
        if (!userId) return [];
        try {
            const response = await apiRequest(`/demo/leaderboard/courses/all?userId=${encodeURIComponent(userId)}`);
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            return await response.json();
        } catch (error) {
            console.error("Ошибка загрузки курсов:", error);
            return [];
        }
    }

    /**
     * Загрузить группы для курса с информацией о членстве пользователя
     * @param {string} courseId - ID курса
     * @param {string} userId - ID пользователя
     * @returns {Promise<Array>} массив групп {groupId, member, ...}
     */
    async function loadGroupsForCourse(courseId, userId) {
        if (!courseId || !userId) return [];
        try {
            const response = await apiRequest(`/demo/leaderboard/courses/${encodeURIComponent(courseId)}/groups?userId=${encodeURIComponent(userId)}`);
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            return await response.json();
        } catch (error) {
            console.error("Ошибка загрузки групп:", error);
            return [];
        }
    }

    // Экспортируем в глобальную область
    window.GamificationAPI = {
        getCurrentUserId,
        setCurrentUserId,
        apiRequest,
        getWidgetConfig,
        STORAGE_USER_ID,
        fetchUsersList,
        loadAllCourses,
        loadGroupsForCourse
    };
})();