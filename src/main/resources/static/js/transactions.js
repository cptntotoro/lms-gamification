/**
 * Скрипт для страницы транзакций
 * - Загружает транзакции для выбранного пользователя (из URL или из хедера)
 * - Поддерживает пагинацию и сортировку
 * - Отображает ошибку доступа для не-ADMIN
 */

(function() {
    // DOM элементы
    const loadingIndicator = document.getElementById('loadingIndicator');
    const errorMessageDiv = document.getElementById('errorMessage');
    const tableContainer = document.getElementById('tableContainer');
    const tbody = document.getElementById('transactionsTableBody');
    const paginationContainer = document.getElementById('paginationContainer');
    const currentUserIdSpan = document.getElementById('currentUserIdSpan');
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    const sortDirSelect = document.getElementById('sortDirSelect');
    const refreshBtn = document.getElementById('refreshBtn');

    // Состояние
    let currentUserId = null;
    let currentPage = 0;
    let currentSize = 20;
    let currentSortDir = 'desc';
    let totalPages = 0;

    // Получить userId из URL параметра или из хедера
    function getUserIdFromUrl() {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get('userId');
    }

    // Загрузить транзакции
    async function loadTransactions(page = 0) {
        if (!currentUserId) {
            showError('Пользователь не определён');
            return;
        }

        currentPage = page;

        // Показываем загрузку
        loadingIndicator.classList.remove('hide');
        tableContainer.classList.add('hide');
        errorMessageDiv.classList.add('hide');

        try {
            const url = `/api/v1/admin/transactions/users/${encodeURIComponent(currentUserId)}?page=${page}&size=${currentSize}&sortDir=${currentSortDir}`;
            const response = await window.GamificationAPI.apiRequest(url, {
                method: 'GET'
            });

            if (response.status === 403) {
                throw new Error('Доступ запрещён. Недостаточно прав для просмотра транзакций (требуется роль ADMIN).');
            }

            if (!response.ok) {
                throw new Error(`Ошибка загрузки: ${response.status} ${response.statusText}`);
            }

            const data = await response.json();
            renderTransactions(data);
        } catch (error) {
            console.error('Ошибка загрузки транзакций:', error);
            showError(error.message);
        } finally {
            loadingIndicator.classList.add('hide');
        }
    }

    // Отрисовать таблицу и пагинацию
    function renderTransactions(data) {
        const content = data.content || [];
        totalPages = data.totalPages || 0;

        if (content.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="no-data">Транзакции отсутствуют</td></tr>';
        } else {
            let html = '';
            content.forEach(tx => {
                const date = tx.createdAt ? new Date(tx.createdAt).toLocaleString() : '—';
                const courseId = tx.courseId || '—';
                const groupId = tx.groupId || '—';
                html += `
                    <tr>
                        <td>${escapeHtml(date)}</td>
                        <td>${escapeHtml(tx.eventId || '—')}</td>
                        <td>${escapeHtml(courseId)}</td>
                        <td>${escapeHtml(groupId)}</td>
                        <td class="text-center">${tx.points !== undefined ? tx.points : '—'}</td>
                        <td>${escapeHtml(tx.description || '—')}</td>
                    </tr>
                `;
            });
            tbody.innerHTML = html;
        }

        renderPagination();
        tableContainer.classList.remove('hide');
    }

    // Отрисовать пагинацию
    function renderPagination() {
        if (totalPages <= 1) {
            paginationContainer.innerHTML = '';
            return;
        }

        let pagesHtml = '';

        // Previous
        const prevDisabled = currentPage === 0;
        pagesHtml += `<a href="#" class="pagination-link ${prevDisabled ? 'disabled' : ''}" data-page="${currentPage - 1}">&laquo; Пред.</a>`;

        // Номера страниц
        const startPage = Math.max(0, currentPage - 2);
        const endPage = Math.min(totalPages - 1, currentPage + 2);
        for (let i = startPage; i <= endPage; i++) {
            const activeClass = i === currentPage ? 'active' : '';
            pagesHtml += `<a href="#" class="pagination-link ${activeClass}" data-page="${i}">${i + 1}</a>`;
        }

        // Next
        const nextDisabled = currentPage >= totalPages - 1;
        pagesHtml += `<a href="#" class="pagination-link ${nextDisabled ? 'disabled' : ''}" data-page="${currentPage + 1}">След. &raquo;</a>`;

        paginationContainer.innerHTML = pagesHtml;

        // Обработчики кликов
        document.querySelectorAll('.pagination-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const page = parseInt(link.getAttribute('data-page'));
                if (isNaN(page) || page < 0 || page >= totalPages) return;
                if (link.classList.contains('disabled')) return;
                loadTransactions(page);
            });
        });
    }

    // Показать сообщение об ошибке
    function showError(msg) {
        errorMessageDiv.classList.remove('hide');
        errorMessageDiv.innerHTML = `<strong>Ошибка:</strong> ${escapeHtml(msg)}`;
        tableContainer.classList.add('hide');
        loadingIndicator.classList.add('hide');
        paginationContainer.innerHTML = '';
    }

    // Экранирование HTML
    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/[&<>]/g, function(m) {
            if (m === '&') return '&amp;';
            if (m === '<') return '&lt;';
            if (m === '>') return '&gt;';
            return m;
        });
    }

    // Обновить параметры и перезагрузить
    function refresh() {
        currentSize = parseInt(pageSizeSelect.value, 10);
        currentSortDir = sortDirSelect.value;
        loadTransactions(0);
    }

    // Инициализация
    async function init() {
        // Получаем userId из URL или из текущего пользователя в хедере
        let userId = getUserIdFromUrl();
        if (!userId) {
            userId = window.GamificationAPI.getCurrentUserId();
        }
        if (!userId) {
            showError('Не удалось определить пользователя. Укажите ?userId=... в адресной строке или выберите пользователя в хедере.');
            return;
        }

        currentUserId = userId;
        if (currentUserIdSpan) currentUserIdSpan.textContent = currentUserId;

        // Установка значений по умолчанию для select'ов
        pageSizeSelect.value = currentSize;
        sortDirSelect.value = currentSortDir;

        // Загружаем транзакции
        await loadTransactions(0);

        // Обработчики
        if (refreshBtn) refreshBtn.addEventListener('click', refresh);
        if (pageSizeSelect) pageSizeSelect.addEventListener('change', refresh);
        if (sortDirSelect) sortDirSelect.addEventListener('change', refresh);
    }

    // Ждём загрузку API
    if (window.GamificationAPI) {
        init();
    } else {
        document.addEventListener('DOMContentLoaded', () => {
            if (window.GamificationAPI) init();
            else showError('GamificationAPI не загружен. Проверьте подключение common-api.js');
        });
    }

    // При смене пользователя в хедере, если нет параметра userId в URL, перезагружаем страницу
    document.addEventListener('userChanged', () => {
        const urlUserId = getUserIdFromUrl();
        if (!urlUserId) {
            // Обновляем userId из хедера без перезагрузки
            const newUserId = window.GamificationAPI.getCurrentUserId();
            if (newUserId !== currentUserId) {
                currentUserId = newUserId;
                if (currentUserIdSpan) currentUserIdSpan.textContent = currentUserId;
                refresh();
            }
        }
    });
})();