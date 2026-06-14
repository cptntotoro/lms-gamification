/**
 * Gamification Widget for LMS
 *
 * Displays user progress, enrolled courses, and course leaderboard in a floating popup.
 *
 * Required configuration (set before calling init function):
 *
 *   window.GamificationWidgetConfig = {
 *     apiBaseUrl: "https://your-gamification-server.com", // base URL of the gamification API
 *     userId: "student123",                                // current user's external ID from LMS
 *     updateIntervalMs: 5000,                              // optional, default 5000ms (5 sec)
 *     headers: {                                           // optional, additional HTTP headers
 *       "X-User-Id": "student123"                         // if required by your API
 *     }
 *   };
 *
 * Then init the widget:
 *   <script src="gamification-widget.js"></script>
 *   <script>window.initGamificationWidget();</script>
 *
 * Or pass config directly:
 *   window.initGamificationWidget({ apiBaseUrl: "...", userId: "..." });
 *
 * The widget will appear in the bottom-right corner.
 */

(function() {
    // ---------- Configuration defaults ----------
    const DEFAULT_UPDATE_INTERVAL_MS = 5000;
    const DEFAULT_PAGE_SIZE = 10;        // for leaderboard top entries
    const MAX_LEADERBOARD_ENTRIES = 10;

    // State that can be cleaned up
    let activeWidget = null;  // holds cleanup function and DOM references

    // ---------- Widget implementation ----------
    function createWidget(configData) {
        // Local copies of config (to avoid external mutation)
        const apiBaseUrl = configData.apiBaseUrl;
        const userId = configData.userId;
        let updateIntervalMs = (configData.updateIntervalMs && !isNaN(configData.updateIntervalMs))
            ? configData.updateIntervalMs
            : DEFAULT_UPDATE_INTERVAL_MS;
        const customHeaders = configData.headers || {};

        // Validation
        if (!apiBaseUrl) {
            console.error("[GamificationWidget] Missing required config: apiBaseUrl");
            return null;
        }
        if (!userId) {
            console.error("[GamificationWidget] Missing required config: userId");
            return null;
        }

        // ---------- State ----------
        let globalData = null;          // UserGlobalCourseGroupDto
        let coursesList = [];           // array of UserCourseSummary
        let leaderboardData = null;     // UserCourseGroupLeaderboardDto for selected course
        let selectedCourseId = null;    // currently selected course for leaderboard tab
        let refreshIntervalId = null;
        let isOpen = false;             // whether the detailed popup is open
        let isLoading = false;
        let activeTab = "overview";     // 'overview', 'courses', 'leaderboard'

        // DOM elements
        let widgetContainer = null;
        let floatingButton = null;
        let popupPanel = null;
        let tabButtons = {};
        let tabContents = {};

        // Helper: build request headers
        function getRequestHeaders() {
            return {
                "Content-Type": "application/json",
                ...customHeaders
            };
        }

        // Helper: safe fetch with timeout
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

        // API calls
        async function fetchGlobalProgress() {
            const url = `${apiBaseUrl}/api/v1/users/${encodeURIComponent(userId)}`;
            const response = await fetchWithTimeout(url, { headers: getRequestHeaders(), credentials: "include" });
            if (!response.ok) throw new Error(`Global progress API error: ${response.status}`);
            const data = await response.json();
            return data;
        }

        async function fetchUserCourses() {
            const url = `${apiBaseUrl}/api/v1/leaderboard/users/${encodeURIComponent(userId)}/courses`;
            const response = await fetchWithTimeout(url, { headers: getRequestHeaders(), credentials: "include" });
            if (!response.ok) throw new Error(`Courses API error: ${response.status}`);
            const data = await response.json();
            return data;
        }

        async function fetchLeaderboard(courseId, page = 0, size = DEFAULT_PAGE_SIZE) {
            const url = `${apiBaseUrl}/api/v1/leaderboard/course/${encodeURIComponent(courseId)}/user/${encodeURIComponent(userId)}?page=${page}&size=${size}`;
            const response = await fetchWithTimeout(url, { headers: getRequestHeaders(), credentials: "include" });
            if (!response.ok) throw new Error(`Leaderboard API error: ${response.status}`);
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

                if (isOpen && activeTab === "leaderboard" && selectedCourseId) {
                    await refreshLeaderboardForSelectedCourse();
                }

            } catch (error) {
                console.error("[GamificationWidget] Refresh error:", error);
                if (isOpen) {
                    showErrorMessage("Ошибка загрузки данных: " + error.message);
                }
            } finally {
                isLoading = false;
                showLoadingState(false);
            }
        }

        async function refreshLeaderboardForSelectedCourse() {
            if (!selectedCourseId) return;
            try {
                const leaderboard = await fetchLeaderboard(selectedCourseId, 0, MAX_LEADERBOARD_ENTRIES);
                leaderboardData = leaderboard;
                if (isOpen && activeTab === "leaderboard") {
                    renderLeaderboardTab();
                }
            } catch (error) {
                console.error("[GamificationWidget] Leaderboard refresh error:", error);
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

        // UI Rendering
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
                html += `
                    <div class="widget-course-item" data-course-id="${courseIdVal}">
                        <div class="widget-course-title">${escapeHtml(displayName)}</div>
                        <div class="widget-course-details">
                            <span>${points} очков</span>
                            <span>${escapeHtml(group)}</span>
                            <span>Дата: ${enrolledAt}</span>
                        </div>
                        <button class="widget-view-leaderboard-btn" data-course-id="${courseIdVal}" data-course-name="${escapeHtml(displayName)}">Посмотреть лидерборд</button>
                    </div>
                `;
            });
            html += `</div>`;
            container.innerHTML = html;

            container.querySelectorAll(".widget-view-leaderboard-btn").forEach(btn => {
                btn.addEventListener("click", (e) => {
                    e.stopPropagation();
                    const cid = btn.getAttribute("data-course-id");
                    const cname = btn.getAttribute("data-course-name");
                    if (cid) {
                        selectedCourseId = cid;
                        activeTab = "leaderboard";
                        activateTab("leaderboard");
                        refreshLeaderboardForSelectedCourse().then(() => {
                            const leaderboardContainer = tabContents.leaderboard;
                            if (leaderboardContainer) {
                                const headerEl = leaderboardContainer.querySelector(".widget-leaderboard-header");
                                if (headerEl) headerEl.textContent = `Лидерборд курса: ${cname || cid}`;
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

            let html = `<div class="widget-leaderboard-header">Лидерборд курса: ${leaderboardData.currentUserEntry.courseDisplayName}</div>`;

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
                    refreshLeaderboardForSelectedCourse();
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
                refreshAllData().catch(e => console.warn("Auto-refresh error", e));
            }, updateIntervalMs);
        }

        function stopAutoRefresh() {
            if (refreshIntervalId) {
                clearInterval(refreshIntervalId);
                refreshIntervalId = null;
            }
        }

        // Build UI and start
        buildWidgetUI();
        startAutoRefresh();
        refreshAllData().catch(console.error);

        // Return cleanup function
        return function cleanup() {
            stopAutoRefresh();
            if (widgetContainer && widgetContainer.parentNode) {
                widgetContainer.parentNode.removeChild(widgetContainer);
            }
            // Remove injected styles
            const styles = document.head.querySelectorAll('style');
            styles.forEach(style => {
                if (style.textContent.includes('.gamification-widget-root')) {
                    style.remove();
                }
            });
        };
    }

    // Expose public API
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