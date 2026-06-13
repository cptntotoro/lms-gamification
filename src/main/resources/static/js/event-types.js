// ======================== ЗАГРУЗКА И ОТОБРАЖЕНИЕ ТАБЛИЦЫ ========================
async function loadEventTypesTable() {
    const loadingIndicator = document.getElementById('loadingIndicator');
    const errorMessageDiv = document.getElementById('errorMessage');
    const tableContainer = document.getElementById('tableContainer');
    const tbody = document.getElementById('eventTypesTableBody');

    try {
        loadingIndicator.style.display = 'block';
        errorMessageDiv.style.display = 'none';
        tableContainer.style.display = 'none';

        const response = await window.GamificationAPI.apiRequest('/demo/admin/event-types/all-types?page=0&size=1000', {
            method: 'GET'
        });

        if (!response.ok) {
            if (response.status === 403) {
                throw new Error('Недостаточно прав для просмотра типов событий. Требуются роли ADMIN или TEACHER.');
            }
            throw new Error(`Ошибка загрузки: ${response.status} ${response.statusText}`);
        }

        const pageData = await response.json();
        const types = pageData.content || [];

        if (types.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="no-data">Типы событий отсутствуют</td></tr>';
        } else {
            let html = '';
            types.forEach(type => {
                const maxDailyPointsText = type.maxDailyPoints ? type.maxDailyPoints : 'Без лимита';
                const statusText = type.active ? 'Активен' : 'Отключён';
                const statusClass = type.active ? 'status-active' : 'status-inactive';
                html += `
                    <tr>
                        <td>${escapeHtml(type.typeCode)}</td>
                        <td>${escapeHtml(type.displayName)}</td>
                        <td>${type.points}</td>
                        <td>${escapeHtml(maxDailyPointsText)}</td>
                        <td><span class="${statusClass}">${statusText}</span></td>
                        <td class="actions-cell">
                            <button class="icon-btn edit" data-id="${type.uuid}" onclick="openEditModal(this)" title="Редактировать тип события">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                                </svg>
                            </button>
                            <button class="icon-btn deactivate" data-id="${type.uuid}" onclick="deactivateType(this)" title="Деактивировать тип события">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M10 11l5 5m0-5l-5 5M6 18l12-12"></path>
                                    <circle cx="12" cy="12" r="10"></circle>
                                </svg>
                            </button>
                        </td>
                    </tr>
                `;
            });
            tbody.innerHTML = html;
        }

        tableContainer.style.display = 'block';
        loadingIndicator.style.display = 'none';
        logEvent('Список типов событий загружен');
    } catch (error) {
        console.error('Ошибка загрузки типов событий:', error);
        loadingIndicator.style.display = 'none';
        errorMessageDiv.style.display = 'block';
        errorMessageDiv.innerHTML = `<strong>Ошибка:</strong> ${error.message}`;
        logEvent(`❌ Ошибка загрузки: ${error.message}`, 'error');
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str.toString().replace(/[&<>]/g, function(m) {
        if (m === '&') return '&amp;';
        if (m === '<') return '&lt;';
        if (m === '>') return '&gt;';
        return m;
    });
}

// ======================== РАБОТА С МОДАЛКОЙ ========================
function openCreateModal() {
    document.getElementById('modalTitle').textContent = 'Создать тип события';
    document.getElementById('eventTypeForm').reset();
    document.getElementById('modalId').value = '';
    document.getElementById('typeCode').disabled = false;
    document.getElementById('eventTypeModal').style.display = 'block';

    logEvent('ADMIN → открыта форма создания типа события');
}

async function openEditModal(btn) {
    let data;
    const id = btn.getAttribute('data-id');

    const getFromLine = () => {
        const row = btn.closest('tr');
        const cells = row.querySelectorAll('td');
        return  {
            typeCode: cells[0].textContent.trim(),
            displayName: cells[1].textContent.trim(),
            points: cells[2].textContent.trim(),
            maxDailyPoints: cells[3].textContent.trim() === 'Без лимита' ? '' : cells[3].textContent.trim(),
            active: cells[4].textContent.trim() === 'Активен'
        }
    };

    try {
        const response = await window.GamificationAPI.apiRequest(`/api/v1/admin/event-types/${id}`, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        });
        data = !response.ok ? getFromLine() : await response.json();
    } catch (e) {
        data = getFromLine();
    }

    document.getElementById('modalTitle').textContent = 'Редактировать тип события';
    document.getElementById('modalId').value = id;
    document.getElementById('typeCode').value = data.typeCode;
    document.getElementById('typeCode').disabled = true; // код менять нельзя
    document.getElementById('displayName').value = data.displayName;
    document.getElementById('points').value = data.points;
    document.getElementById('maxDailyPoints').value = data.maxDailyPoints;
    document.getElementById('active').checked = data.active;

    document.getElementById('eventTypeModal').style.display = 'block';
    logEvent(`ADMIN → редактирует тип "${data.displayName}" (id=${id})`);
}

function closeModal() {
    document.getElementById('eventTypeModal').style.display = 'none';
}

// ======================== ДЕАКТИВАЦИЯ (DELETE) ========================
async function deactivateType(btn) {
    const id = btn.getAttribute('data-id');
    if (!confirm('Деактивировать тип события?')) return;

    try {
        const response = await window.GamificationAPI.apiRequest(`/api/v1/admin/event-types/${id}`, { method: 'DELETE' });
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `Ошибка ${response.status}`);
        }
        logEvent(`ADMIN → деактивировал тип события (id=${id})`);
        // Перезагружаем таблицу без перезагрузки страницы
        await loadEventTypesTable();
    } catch (error) {
        logEvent(`❌ Ошибка: ${error.message}`);
        alert('Ошибка: ' + error.message);
    }
}

// ======================== СОХРАНЕНИЕ (POST / PUT) ========================
async function saveEventType() {
    const form = document.getElementById('eventTypeForm');
    const id = document.getElementById('modalId').value;
    const isEdit = !!id;

    // Валидация обязательных полей
    const displayName = form.displayName.value.trim();
    if (!displayName) {
        alert('Введите название типа события');
        return;
    }

    const points = parseInt(form.points.value, 10);
    if (isNaN(points)) {
        alert('Количество очков должно быть числом');
        return;
    }

    let maxDailyPoints = form.maxDailyPoints.value.trim();
    if (maxDailyPoints !== '') {
        maxDailyPoints = parseInt(maxDailyPoints, 10);
        if (isNaN(maxDailyPoints)) {
            alert('Максимум баллов в день должен быть целым числом или пустым');
            return;
        }
    } else {
        maxDailyPoints = null;
    }

    // Формируем тело запроса
    const data = {
        displayName,
        points,
        maxDailyPoints,
    };

    let url = '/api/v1/admin/event-types';
    let method = 'POST';

    if (isEdit) {
        url = `/api/v1/admin/event-types/${id}`;
        method = 'PUT';
        data.active = form.active.checked;
    } else {
        const typeCode = form.typeCode.value.trim();
        if (!typeCode) {
            alert('Укажите код типа события');
            return;
        }
        data.typeCode = typeCode;
    }

    try {
        const response = await window.GamificationAPI.apiRequest(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            let errorText = 'Ошибка сохранения';

            try {
                const err = await response.json();
                errorText = err.message || err.error || JSON.stringify(err);
            } catch (e) {
                errorText = response.statusText;
            }

            throw new Error(errorText);
        }

        closeModal();
        logEvent(`ADMIN → ${isEdit ? 'обновил' : 'создал'} тип "${displayName}"`);
        // Перезагружаем таблицу
        await loadEventTypesTable();
    } catch (error) {
        logEvent(`❌ Ошибка: ${error.message}`);
        alert('Ошибка: ' + error.message);
    }
}

window.onclick = function (event) {
    const modal = document.getElementById('eventTypeModal');
    if (event.target === modal) closeModal();
};

function logEvent(message, type = 'info') {
    // Можно оставить для отладки в консоли
    console.log(`[${new Date().toLocaleTimeString()}] ${message}`);
}

// Загружаем таблицу при загрузке страницы
document.addEventListener('DOMContentLoaded', () => {
    loadEventTypesTable();
});

// При смене пользователя/роли обновляем таблицу
document.addEventListener('userChanged', () => {
    loadEventTypesTable();
});