/**
 * Общий модуль для API-запросов
 * Предоставляет единую конфигурацию заголовков и работу с текущим пользователем/ролью.
 */

(function() {
    // Ключи localStorage
    const STORAGE_USER_ID = "demoUserId";
    const STORAGE_ROLE = "demoRole";

    // Значения по умолчанию
    const DEFAULT_USER_ID = "student001";
    const DEFAULT_ROLE = "ADMIN";

    // Токен для аутентификации (фиксированный для демо)
    const AUTH_TOKEN = "asdasd2123sadadad21asdasd";

    // --- Геттеры / сеттеры ---
    function getCurrentUserId() {
        return localStorage.getItem(STORAGE_USER_ID) || DEFAULT_USER_ID;
    }

    function setCurrentUserId(userId) {
        localStorage.setItem(STORAGE_USER_ID, userId);
    }

    function getCurrentRole() {
        return localStorage.getItem(STORAGE_ROLE) || DEFAULT_ROLE;
    }

    function setCurrentRole(role) {
        localStorage.setItem(STORAGE_ROLE, role);
    }

    /**
     * Выполнить API-запрос с автоматической подстановкой заголовков
     * @param {string} url - URL (абсолютный или относительный)
     * @param {Object} options - параметры fetch (method, body, headers и т.д.)
     * @param {Object} options.overrideHeaders - дополнительные заголовки (переопределяют стандартные)
     * @param {boolean} options.useAdminRole - если true, в заголовке X-Role будет "ADMIN" независимо от текущей роли
     * @returns {Promise<Response>}
     */
    async function apiRequest(url, options = {}) {
        const { overrideHeaders = {}, useAdminRole = false, ...fetchOptions } = options;

        const headers = {
            "Content-Type": "application/json",
            "X-User-Id": getCurrentUserId(),
            "X-Role": useAdminRole ? "ADMIN" : getCurrentRole(),
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
            apiBaseUrl: window.location.origin, // или можно задать явно "http://localhost:8080"
            userId: getCurrentUserId(),
            updateIntervalMs: 5000,
            headers: {
                "X-User-Id": getCurrentUserId(),
                "X-Role": getCurrentRole(),
                "token": AUTH_TOKEN
            }
        };
    }

    // Экспортируем в глобальную область
    window.GamificationAPI = {
        getCurrentUserId,
        setCurrentUserId,
        getCurrentRole,
        setCurrentRole,
        apiRequest,
        getWidgetConfig,
        STORAGE_USER_ID,
        STORAGE_ROLE
    };
})();