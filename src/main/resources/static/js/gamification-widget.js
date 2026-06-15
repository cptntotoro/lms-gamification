/**
 * Виджет геймификации для LMS
 *
 * Отображает прогресс пользователя, записанные курсы и лидерборд курса во всплывающем окне.
 *
 * Требуемая конфигурация (задать до вызова init):
 *
 *   window.GamificationWidgetConfig = {
 *     apiBaseUrl: "https://your-gamification-server.com", // базовый URL API геймификации
 *     userId: "student123",                                // внешний ID текущего пользователя из LMS
 *     updateIntervalMs: 5000,                              // опционально, по умолчанию 5000 мс (5 сек)
 *     headers: {                                           // опционально, дополнительные HTTP-заголовки
 *       "X-User-Id": "student123"                         // если требуется вашим API
 *     }
 *   };
 *
 * Затем инициализировать виджет:
 *   <script src="gamification-widget.js"></script>
 *   <script>window.initGamificationWidget();</script>
 *
 * Или передать конфиг напрямую:
 *   window.initGamificationWidget({ apiBaseUrl: "...", userId: "..." });
 *
 * Виджет появится в правом нижнем углу.
 */

(function() {
    // ---------- Значения по умолчанию ----------
    const DEFAULT_UPDATE_INTERVAL_MS = 5000;
    const DEFAULT_PAGE_SIZE = 10;        // для верхних записей лидерборда
    const MAX_LEADERBOARD_ENTRIES = 10;

    // Состояние для очистки
    let activeWidget = null;  // хранит функцию очистки и ссылки на DOM

    // ---------- Реализация виджета ----------
    function createWidget(configData) {
        // Локальные копии конфигурации (чтобы избежать внешних мутаций)
        const apiBaseUrl = configData.apiBaseUrl;
        const userId = configData.userId;
        let updateIntervalMs = (configData.updateIntervalMs && !isNaN(configData.updateIntervalMs))
            ? configData.updateIntervalMs
            : DEFAULT_UPDATE_INTERVAL_MS;
        const customHeaders = configData.headers || {};

        // Проверка обязательных параметров
        if (!apiBaseUrl) {
            console.error("[GamificationWidget] Отсутствует обязательный параметр конфигурации: apiBaseUrl");
            return null;
        }
        if (!userId) {
            console.error("[GamificationWidget] Отсутствует обязательный параметр конфигурации: userId");
            return null;
        }

        // ---------- Состояние ----------
        let globalData = null;          // UserGlobalCourseGroupDto
        let coursesList = [];           // массив UserCourseSummary
        let leaderboardData = null;     // UserCourseGroupLeaderboardDto для выбранного курса
        let selectedCourseId = null;    // выбранный курс для вкладки лидерборда
        let selectedGroupId = null;     // выбранная группа (null = лидерборд курса)
        let refreshIntervalId = null;
        let isOpen = false;             // открыта ли панель с подробностями
        let isLoading = false;
        let activeTab = "overview";     // 'overview', 'courses', 'leaderboard'

        // DOM-элементы
        let widgetContainer = null;
        let floatingButton = null;
        let popupPanel = null;
        let tabButtons = {};
        let tabContents = {};

        // Вспомогательная функция: формирование заголовков запроса
        function getRequestHeaders() {
            return {
                "Content-Type": "application/json",
                ...customHeaders
            };
        }

        // Вспомогательная функция: безопасный fetch с таймаутом
        async function fetchWithTimeout(url, options, timeoutMs = 10000) {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), timeoutMs);
            try {
                const response = await fetch(url, { ...options, signal: controller.signal });
                clearTimeout(timeoutId);
                return response;
            } catch (error) {
                clearTimeout(timeoutId);
                throw error;
            }
        }

        // API-вызовы
        async function fetchGlobalProgress() {
            const url = `${apiBaseUrl}/api/v1/users/${encodeURIComponent(userId)}`;
            const response = await fetchWithTimeout(url, { headers: getRequestHeaders(), credentials: "include" });
            if (!response.ok) throw new Error(`Ошибка API глобального прогресса: ${response.status}`);
            const data = await response.json();
            return data;
        }

        async function fetchUserCourses() {
            const url = `${apiBaseUrl}/api/v1/leaderboard/users/${encodeURIComponent(userId)}/courses`;
            const response = await fetchWithTimeout(url, { headers: getRequestHeaders(), credentials: "include" });
            if (!response.ok) throw new Error(`Ошибка API курсов: ${response.status}`);
            const data = await response.json();
            return data;
        }

        async function fetchLeaderboard(courseId, groupId = null, page = 0, size = DEFAULT_PAGE_SIZE) {
            let url = `${apiBaseUrl}/api/v1/leaderboard/course/${encodeURIComponent(courseId)}/user/${encodeURIComponent(userId)}?page=${page}&size=${size}`;
            if (groupId) {
                url += `&groupId=${encodeURIComponent(groupId)}`;
            }
            const response = await fetchWithTimeout(url, { headers: getRequestHeaders(), credentials: "include" });
            if (!response.ok) throw new Error(`Ошибка API лидерборда: ${response.status}`);
            const data = await response.json();
            return data;
        }

        async function refreshAllData() {
            if (isLoading) return;
            isLoading = true;
            showLoadingState(true);

            try {
                const [global, coursesResponse] = await Promise.all([
                    fetchGlobalProgress(),
                    fetchUserCourses()
                ]);

                globalData = global;
                coursesList = coursesResponse.courses || [];

                updateFloatingButtonInfo();

                if (isOpen) {
                    renderCurrentTab();
                }

                // Обновляем лидерборд, если открыта соответствующая вкладка
                if (isOpen && activeTab === "leaderboard" && selectedCourseId) {
                    await refreshLeaderboardForCurrentFilter();
                }

            } catch (error) {
                console.error("[GamificationWidget] Ошибка обновления:", error);
                if (isOpen) {
                    showErrorMessage("Ошибка загрузки данных: " + error.message);
                }
            } finally {
                isLoading = false;
                showLoadingState(false);
            }
        }

        // Обновление лидерборда с учётом текущей группы (selectedGroupId)
        async function refreshLeaderboardForCurrentFilter() {
            if (!selectedCourseId) return;
            try {
                const leaderboard = await fetchLeaderboard(selectedCourseId, selectedGroupId, 0, MAX_LEADERBOARD_ENTRIES);
                leaderboardData = leaderboard;
                if (isOpen && activeTab === "leaderboard") {
                    renderLeaderboardTab();
                }
            } catch (error) {
                console.error("[GamificationWidget] Ошибка обновления лидерборда:", error);
                if (isOpen && activeTab === "leaderboard") {
                    showErrorMessage("Ошибка загрузки лидерборда: " + error.message);
                }
            }
        }

        function updateFloatingButtonInfo() {
            if (!floatingButton) return;
            if (globalData) {
                const points = globalData.totalPoints !== undefined ? globalData.totalPoints : 0;
                const level = globalData.level !== undefined ? globalData.level : 0;
                floatingButton.innerHTML = `
                    <div style="display: flex; align-items: center; gap: 6px;">
                        <span>XP</span>
                        <span style="font-weight: bold;">${points}</span>
                        <span style="font-size: 0.9em;">Lv${level}</span>
                    </div>
                `;
            } else {
                floatingButton.innerHTML = `<span>XP</span>`;
            }
        }

        // Отрисовка UI
        function showLoadingState(loading) {
            if (!popupPanel) return;
            const loaderDiv = popupPanel.querySelector(".widget-loader-overlay");
            if (loaderDiv) {
                if (loading) loaderDiv.classList.remove('hide');
                else loaderDiv.classList.add('hide');
            }
        }

        function showErrorMessage(msg) {
            if (!popupPanel) return;
            const contentDiv = popupPanel.querySelector(".widget-tab-content.active");
            if (contentDiv) {
                const errorDiv = document.createElement("div");
                errorDiv.className = "widget-error";
                errorDiv.innerText = msg;
                const existing = contentDiv.querySelector(".widget-error");
                if (existing) existing.remove();
                contentDiv.prepend(errorDiv);
                setTimeout(() => errorDiv.remove(), 5000);
            }
        }

        function renderCurrentTab() {
            if (!isOpen || !popupPanel) return;
            if (activeTab === "overview") renderOverviewTab();
            else if (activeTab === "courses") renderCoursesTab();
            else if (activeTab === "leaderboard") renderLeaderboardTab();
        }

        function renderOverviewTab() {
            const container = tabContents.overview;
            if (!container) return;
            if (!globalData) {
                container.innerHTML = `<div class="widget-loading">Загрузка...</div>`;
                return;
            }

            const totalPoints = globalData.totalPoints || 0;
            const level = globalData.level || 0;
            const pointsToNext = globalData.pointsToNextLevel || 0;
            const progressPercent = globalData.progressPercent || 0;

            container.innerHTML = `
                <div class="widget-stat-card">
                    <div class="widget-stat-row">
                        <span class="widget-stat-label">Всего очков:</span>
                        <span class="widget-stat-value">${totalPoints}</span>
                    </div>
                    <div class="widget-stat-row">
                        <span class="widget-stat-label">Уровень:</span>
                        <span class="widget-stat-value">${level}</span>
                    </div>
                    <div class="widget-stat-row">
                        <span class="widget-stat-label">До следующего уровня:</span>
                        <span class="widget-stat-value">${pointsToNext} очков</span>
                    </div>
                    <div class="widget-progress-container">
                        <div class="widget-progress-bar" style="width: ${Math.min(100, progressPercent)}%;"></div>
                        <span class="widget-progress-text">${progressPercent.toFixed(1)}%</span>
                    </div>
                </div>
                <div class="widget-note">Общий прогресс по всем курсам</div>
            `;
        }

        function renderCoursesTab() {
            const container = tabContents.courses;
            if (!container) return;
            if (!coursesList || coursesList.length === 0) {
                container.innerHTML = `<div class="widget-empty">Нет курсов или данные не загружены</div>`;
                return;
            }

            let html = `<div class="widget-courses-list">`;
            coursesList.forEach(course => {
                const courseIdVal = course.courseId || "—";
                const displayName = course.displayName || courseIdVal;
                const points = course.totalPointsInCourse || 0;
                const group = course.groupId ? `Группа: ${course.groupId}` : "Без группы";
                const enrolledAt = course.enrolledAt ? new Date(course.enrolledAt).toLocaleDateString() : "—";
                // Проверяем, состоит ли пользователь в группе на этом курсе
                const userGroupId = course.groupId; // предполагаем, что поле groupId есть в ответе API
                html += `
                    <div class="widget-course-item" data-course-id="${courseIdVal}">
                        <div class="widget-course-title">${escapeHtml(displayName)}</div>
                        <div class="widget-course-details">
                            <span>${points} очков</span>
                            <span>${escapeHtml(group)}</span>
                            <span>Дата: ${enrolledAt}</span>
                        </div>
                        <div style="display: flex; gap: 8px; margin-top: 8px;">
                            <button class="widget-view-leaderboard-btn course" data-course-id="${courseIdVal}" data-course-name="${escapeHtml(displayName)}">Посмотреть лидерборд курса</button>
                            ${userGroupId ? `<button class="widget-view-leaderboard-btn group" data-course-id="${courseIdVal}" data-group-id="${escapeHtml(userGroupId)}" data-course-name="${escapeHtml(displayName)}" data-group-name="${escapeHtml(userGroupId)}">Посмотреть лидерборд группы (${escapeHtml(userGroupId)})</button>` : ''}
                        </div>
                    </div>
                `;
            });
            html += `</div>`;
            container.innerHTML = html;

            // Обработчики для кнопок "Посмотреть лидерборд курса"
            container.querySelectorAll(".widget-view-leaderboard-btn.course").forEach(btn => {
                btn.addEventListener("click", (e) => {
                    e.stopPropagation();
                    const cid = btn.getAttribute("data-course-id");
                    const cname = btn.getAttribute("data-course-name");
                    if (cid) {
                        selectedCourseId = cid;
                        selectedGroupId = null; // сбрасываем группу
                        activeTab = "leaderboard";
                        activateTab("leaderboard");
                        refreshLeaderboardForCurrentFilter().then(() => {
                            const leaderboardContainer = tabContents.leaderboard;
                            if (leaderboardContainer) {
                                const headerEl = leaderboardContainer.querySelector(".widget-leaderboard-header");
                                if (headerEl) headerEl.textContent = `Лидерборд курса: ${cname || cid}`;
                            }
                        });
                    }
                });
            });

            // Обработчики для кнопок "Посмотреть лидерборд группы"
            container.querySelectorAll(".widget-view-leaderboard-btn.group").forEach(btn => {
                btn.addEventListener("click", (e) => {
                    e.stopPropagation();
                    const cid = btn.getAttribute("data-course-id");
                    const gid = btn.getAttribute("data-group-id");
                    const cname = btn.getAttribute("data-course-name");
                    const gname = btn.getAttribute("data-group-name");
                    if (cid && gid) {
                        selectedCourseId = cid;
                        selectedGroupId = gid; // устанавливаем группу
                        activeTab = "leaderboard";
                        activateTab("leaderboard");
                        refreshLeaderboardForCurrentFilter().then(() => {
                            const leaderboardContainer = tabContents.leaderboard;
                            if (leaderboardContainer) {
                                const headerEl = leaderboardContainer.querySelector(".widget-leaderboard-header");
                                if (headerEl) headerEl.textContent = `Лидерборд группы "${gname}" курса: ${cname || cid}`;
                            }
                        });
                    }
                });
            });
        }

        function renderLeaderboardTab() {
            const container = tabContents.leaderboard;
            if (!container) return;
            if (!selectedCourseId) {
                container.innerHTML = `<div class="widget-empty">Выберите курс во вкладке "Мои курсы", чтобы увидеть лидерборд.</div>`;
                return;
            }
            if (!leaderboardData) {
                container.innerHTML = `<div class="widget-loading">Загрузка лидерборда...</div>`;
                return;
            }

            const topEntries = leaderboardData.topEntries || [];
            const currentUserRank = leaderboardData.currentUserRank;
            const currentUserPoints = leaderboardData.currentUserPoints;
            const isGroupLeaderboard = selectedGroupId !== null;

            let html = `<div class="widget-leaderboard-header">${isGroupLeaderboard ? `Лидерборд группы "${selectedGroupId}"` : `Лидерборд курса`}: ${leaderboardData.currentUserEntry.courseDisplayName}</div>`;

            html += `<div class="widget-current-user-stats">
                        <div>Ваше место: ${currentUserRank !== null && currentUserRank !== undefined ? currentUserRank : "—"}</div>
                        <div>Ваши очки на курсе: ${currentUserPoints !== null ? currentUserPoints : "—"}</div>
                    </div>`;

            html += `<div class="widget-leaderboard-list"><table class="widget-leaderboard-table">
                        <thead><tr><th>#</th><th>Пользователь</th><th>Очки</th><th>Уровень</th></tr></thead><tbody>`;
            topEntries.forEach(entry => {
                const rank = entry.rank || "—";
                const userIdDisplay = entry.userId || "—";
                const points = entry.pointsInCourse || 0;
                const level = entry.globalLevel || 0;
                const isCurrent = entry.isCurrentUser === true;
                html += `<tr class="${isCurrent ? 'widget-current-row' : ''}">
                              <td>${rank}</td>
                              <td>${escapeHtml(userIdDisplay)}${isCurrent ? ' (Вы)' : ''}</td>
                              <td>${points}</td>
                              <td>${level}</td>
                            </tr>`;
            });
            html += `</tbody></table></div>`;
            if (topEntries.length === 0) {
                html += `<div class="widget-empty">Нет участников для отображения</div>`;
            }
            container.innerHTML = html;
        }

        function escapeHtml(str) {
            if (!str) return "";
            return str.replace(/[&<>]/g, function(m) {
                if (m === '&') return '&amp;';
                if (m === '<') return '&lt;';
                if (m === '>') return '&gt;';
                return m;
            });
        }

        function activateTab(tabId) {
            activeTab = tabId;
            for (let [id, btn] of Object.entries(tabButtons)) {
                if (id === tabId) {
                    btn.classList.add("widget-tab-active");
                } else {
                    btn.classList.remove("widget-tab-active");
                }
            }
            for (let [id, content] of Object.entries(tabContents)) {
                if (id === tabId) {
                    content.classList.add("active");
                } else {
                    content.classList.remove("active");
                }
            }
            renderCurrentTab();
        }

        function buildWidgetUI() {
            widgetContainer = document.createElement("div");
            widgetContainer.id = "gamification-widget-root";
            widgetContainer.className = "gamification-widget-root";
            document.body.appendChild(widgetContainer);

            floatingButton = document.createElement("div");
            floatingButton.className = "widget-floating-button";
            floatingButton.innerHTML = `<span>XP</span>`;
            floatingButton.addEventListener("click", togglePopup);
            widgetContainer.appendChild(floatingButton);

            popupPanel = document.createElement("div");
            popupPanel.className = "widget-popup-panel hide";

            const headerDiv = document.createElement("div");
            headerDiv.className = "widget-popup-header";
            headerDiv.innerHTML = `<span>Прогресс геймификации</span>
                                    <button class="widget-close-btn">✕</button>`;
            popupPanel.appendChild(headerDiv);

            const tabsDiv = document.createElement("div");
            tabsDiv.className = "widget-tabs";
            const overviewBtn = createTabButton("overview", "Общий прогресс");
            const coursesBtn = createTabButton("courses", "Мои курсы");
            const leaderboardBtn = createTabButton("leaderboard", "Лидерборд курса");
            tabsDiv.appendChild(overviewBtn);
            tabsDiv.appendChild(coursesBtn);
            tabsDiv.appendChild(leaderboardBtn);
            popupPanel.appendChild(tabsDiv);

            const contentContainer = document.createElement("div");
            contentContainer.className = "widget-content-container";

            const overviewContent = document.createElement("div");
            overviewContent.className = "widget-tab-content hide";
            overviewContent.id = "widget-tab-overview";
            const coursesContent = document.createElement("div");
            coursesContent.className = "widget-tab-content hide";
            coursesContent.id = "widget-tab-courses";
            const leaderboardContent = document.createElement("div");
            leaderboardContent.className = "widget-tab-content hide";
            leaderboardContent.id = "widget-tab-leaderboard";

            [overviewContent, coursesContent, leaderboardContent].forEach(div => {
                contentContainer.appendChild(div);
            });
            popupPanel.appendChild(contentContainer);

            const loaderOverlay = document.createElement("div");
            loaderOverlay.className = "widget-loader-overlay hide";
            loaderOverlay.innerHTML = `<div class="widget-loader-spinner">Загрузка...</div>`;
            popupPanel.appendChild(loaderOverlay);

            widgetContainer.appendChild(popupPanel);

            tabButtons = { overview: overviewBtn, courses: coursesBtn, leaderboard: leaderboardBtn };
            tabContents = { overview: overviewContent, courses: coursesContent, leaderboard: leaderboardContent };

            popupPanel.querySelector(".widget-close-btn").addEventListener("click", () => {
                closePopup();
            });

            activateTab("overview");
        }

        function createTabButton(tabId, label) {
            const btn = document.createElement("button");
            btn.innerText = label;
            btn.className = "widget-tab-btn";
            btn.addEventListener("click", () => {
                if (activeTab === tabId) return;
                activeTab = tabId;
                activateTab(tabId);
                if (tabId === "leaderboard" && selectedCourseId) {
                    refreshLeaderboardForCurrentFilter();
                }
            });
            return btn;
        }

        function togglePopup() {
            if (isOpen) {
                closePopup();
            } else {
                openPopup();
            }
        }

        function openPopup() {
            if (!popupPanel) return;
            popupPanel.classList.remove('hide');
            isOpen = true;
            refreshAllData();
        }

        function closePopup() {
            if (!popupPanel) return;
            popupPanel.classList.add('hide');
            isOpen = false;
        }

        function startAutoRefresh() {
            if (refreshIntervalId) clearInterval(refreshIntervalId);
            refreshIntervalId = setInterval(() => {
                refreshAllData().catch(e => console.warn("Ошибка автообновления", e));
            }, updateIntervalMs);
        }

        function stopAutoRefresh() {
            if (refreshIntervalId) {
                clearInterval(refreshIntervalId);
                refreshIntervalId = null;
            }
        }

        // Создаём UI и запускаем
        buildWidgetUI();
        startAutoRefresh();
        refreshAllData().catch(console.error);

        // Возвращаем функцию очистки
        return function cleanup() {
            stopAutoRefresh();
            if (widgetContainer && widgetContainer.parentNode) {
                widgetContainer.parentNode.removeChild(widgetContainer);
            }
            // Удаляем инжектированные стили
            const styles = document.head.querySelectorAll('style');
            styles.forEach(style => {
                if (style.textContent.includes('.gamification-widget-root')) {
                    style.remove();
                }
            });
        };
    }

    // Публичное API
    window.initGamificationWidget = function(providedConfig) {
        if (providedConfig) {
            window.GamificationWidgetConfig = providedConfig;
        }
        const config = window.GamificationWidgetConfig || {};

        if (window.__gamificationWidgetCleanup) {
            window.__gamificationWidgetCleanup();
            window.__gamificationWidgetCleanup = null;
        }

        const cleanup = createWidget(config);
        if (cleanup) {
            window.__gamificationWidgetCleanup = cleanup;
        }
    };

    if (window.GamificationWidgetConfig && window.GamificationWidgetConfig.apiBaseUrl && window.GamificationWidgetConfig.userId) {
        window.initGamificationWidget();
    }
})();