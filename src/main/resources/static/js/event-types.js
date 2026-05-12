// ======================== ВСПОМОГАТЕЛЬНАЯ ФУНКЦИЯ API ========================
/**
 * Универсальная функция для запросов к API
 * @param {string} url - относительный или абсолютный путь
 * @param {Object} options - параметры fetch (method, body, headers и т.д.)
 * @returns {Promise<Response>} - fetch ответ
 */
async function api(url, options = {}) {
    // Базовые заголовки по умолчанию
    const defaultHeaders = {
        'Content-Type': 'application/json',
        "X-User-Id": localStorage.getItem("demoUserId") || "student001",
        "X-Role": localStorage.getItem("demoRole") || "ADMIN",
    };

    const config = {
        ...options,
        headers: {
            ...defaultHeaders,
            ...options.headers,
        },
    };

    try {
        const response = await fetch(url, config);
        return response;
    } catch (error) {
        console.error('API fetch error:', error);
        throw new Error('Сетевая ошибка: ' + error.message);
    }
}

// ======================== ОТКРЫТИЕ / ЗАКРЫТИЕ МОДАЛКИ ========================
function openCreateModal() {
    document.getElementById('modalTitle').textContent = 'Создать тип события';
    document.getElementById('eventTypeForm').reset();
    document.getElementById('modalId').value = '';
    document.getElementById('typeCode').disabled = false;
    document.getElementById('eventTypeModal').style.display = 'block';

    logEvent('ADMIN → открыта форма создания типа события');
}

function openEditModal(btn) {
    const row = btn.closest('tr');
    const cells = row.querySelectorAll('td');

    const typeCode = cells[0].textContent.trim();
    const displayName = cells[1].textContent.trim();
    const points = cells[2].textContent.trim();
    let maxDaily = cells[3].textContent.trim();
    const activeText = cells[4].textContent.trim();

    // Преобразуем "Без лимита" в пустую строку для поля ввода
    maxDaily = maxDaily === 'Без лимита' ? '' : maxDaily;

    const active = activeText === 'Активен';

    document.getElementById('modalTitle').textContent = 'Редактировать тип события';
    document.getElementById('modalId').value = btn.getAttribute('data-id');
    document.getElementById('typeCode').value = typeCode;
    document.getElementById('typeCode').disabled = true; // код менять нельзя
    document.getElementById('displayName').value = displayName;
    document.getElementById('points').value = points;
    document.getElementById('maxDailyPoints').value = maxDaily;
    document.getElementById('active').checked = active;

    document.getElementById('eventTypeModal').style.display = 'block';
    logEvent(`ADMIN → редактирует тип "${displayName}" (id=${btn.getAttribute('data-id')})`);
}

function closeModal() {
    document.getElementById('eventTypeModal').style.display = 'none';
}

// ======================== ДЕАКТИВАЦИЯ (DELETE) ========================
async function deactivateType(btn) {
    const id = btn.getAttribute('data-id');
    if (!confirm('Деактивировать тип события?')) return;

    try {
        const response = await api(`/api/admin/event-types/${id}`, { method: 'DELETE' });
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `Ошибка ${response.status}`);
        }
        logEvent(`ADMIN → деактивировал тип события (id=${id})`);
        location.reload(); // или можно удалить строку из таблицы без перезагрузки
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

    let url = '/api/admin/event-types';
    let method = 'POST';

    if (isEdit) {
        url = `/api/admin/event-types/${id}`;
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
        const response = await api(url, {
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
        location.reload();
    } catch (error) {
        logEvent(`❌ Ошибка: ${error.message}`);
        alert('Ошибка: ' + error.message);
    }
}

window.onclick = function (event) {
    const modal = document.getElementById('eventTypeModal');
    if (event.target === modal) closeModal();
};

function logEvent(message) {
    const logContainer = document.getElementById('eventLog');
    if (!logContainer) return;

    const time = new Date().toLocaleTimeString();

    const item = document.createElement('div');
    item.className = 'log-item';
    item.textContent = `[${time}] ${message}`;

    logContainer.prepend(item);
}