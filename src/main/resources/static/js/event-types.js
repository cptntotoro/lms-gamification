// Открытие модалки для создания
function openCreateModal() {
    document.getElementById('modalTitle').textContent = 'Создать тип события';
    document.getElementById('eventTypeForm').reset();
    document.getElementById('modalId').value = '';
    document.getElementById('typeCode').disabled = false;
    document.getElementById('eventTypeModal').style.display = 'block';
}

// Открытие модалки для редактирования — данные берём из строки таблицы
function openEditModal(btn) {
    const row = btn.closest('tr'); // находим строку таблицы

    // Извлекаем данные из ячеек (td)
    const cells = row.querySelectorAll('td');
    const typeCode = cells[0].textContent.trim(); // Код
    const displayName = cells[1].textContent.trim(); // Название
    const points = cells[2].textContent.trim(); // Очки
    const maxDaily = cells[3].textContent.trim() === 'Без лимита' ? '' : cells[3].textContent.trim(); // Лимит
    const activeText = cells[4].textContent.trim(); // Статус
    const active = activeText === 'Активен';

    // Заполняем форму
    document.getElementById('modalTitle').textContent = 'Редактировать тип события';
    document.getElementById('modalId').value = btn.getAttribute('data-id');
    document.getElementById('typeCode').value = typeCode;
    document.getElementById('typeCode').disabled = true; // нельзя менять код
    document.getElementById('displayName').value = displayName;
    document.getElementById('points').value = points;
    document.getElementById('maxDailyPoints').value = maxDaily;
    document.getElementById('active').checked = active;

    document.getElementById('eventTypeModal').style.display = 'block';
}

function closeModal() {
    document.getElementById('eventTypeModal').style.display = 'none';
}

async function deactivateType(btn) {
    const id = btn.getAttribute('data-id');
    if (!confirm('Деактивировать тип события?')) return;

    try {
        const response = await fetch(`/api/admin/event-types/${id}`, {method: 'DELETE'});
        if (!response.ok) throw new Error('Ошибка деактивации');
        location.reload();
    } catch (error) {
        alert('Не удалось деактивировать: ' + error.message);
    }
}

async function saveEventType() {
    const form = document.getElementById('eventTypeForm');
    const id = document.getElementById('modalId').value;
    const isEdit = !!id;
    const url = isEdit ? `/api/admin/event-types/${id}` : '/api/admin/event-types';
    const method = isEdit ? 'PUT' : 'POST';

    const data = {
        displayName: form.displayName.value.trim(),
        points: parseInt(form.points.value),
        maxDailyPoints: form.maxDailyPoints.value ? parseInt(form.maxDailyPoints.value) : null,
        active: form.active.checked
    };

    if (!isEdit) {
        data.typeCode = form.typeCode.value.trim();
        if (!data.typeCode) {
            alert('Укажите код типа события');
            return;
        }
    }

    try {
        const response = await fetch(url, {
            method,
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || 'Ошибка сохранения');
        }

        closeModal();
        location.reload();
    } catch (error) {
        alert('Ошибка: ' + error.message);
    }
}

window.onclick = function (event) {
    const modal = document.getElementById('eventTypeModal');
    if (event.target === modal) closeModal();
}