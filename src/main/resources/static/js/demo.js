// ============================
// Глобальные переменные и данные
// ============================

let availableCourses = [];            // все курсы {courseId, enrolled, totalPointsInCourse}
let availableEventTypes = [];         // активные типы событий
let lastSentEventId = null;           // для дублирования, храним в localStorage

// DOM элементы
let logContainer;

// Ключ localStorage для lastEventId
const STORAGE_LAST_EVENT_ID = "demoLastEventId";

// ============================
// Вспомогательные функции (лог, очистка)
// ============================
function log(message, type = 'info') {
    if (!logContainer) return;
    const time = new Date().toLocaleTimeString();
    const entry = document.createElement('div');
    entry.className = `log-item log-${type}`;
    entry.innerHTML = `<span>[${time}]</span> ${message}`;
    logContainer.prepend(entry);
    while (logContainer.children.length > 200) {
        logContainer.removeChild(logContainer.lastChild);
    }
}

function clearLog() {
    if (logContainer) logContainer.innerHTML = '';
    log('Лог очищен', 'info');
}

// Загрузка ВСЕХ курсов (с признаком записи для текущего пользователя)
async function loadAllCourses() {
    try {
        const userId = window.GamificationAPI.getCurrentUserId();
        const response = await window.GamificationAPI.apiRequest(`/demo/leaderboard/courses/all?userId=${encodeURIComponent(userId)}`);
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        availableCourses = await response.json();
        log(`Загружено курсов: ${availableCourses.length}`, 'success');
    } catch (error) {
        log(`Ошибка загрузки курсов: ${error.message}`, 'error');
        availableCourses = [];
    }
}

// Загрузка активных типов событий
async function loadEventTypes() {
    try {
        const response = await window.GamificationAPI.apiRequest(`/demo/admin/event-types/all-types?page=0&size=100`);
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const page = await response.json();
        availableEventTypes = page.content.filter(t => t.active);
        log(`Загружено типов событий: ${availableEventTypes.length}`, 'success');
    } catch (error) {
        log(`Ошибка загрузки типов событий: ${error.message}`, 'error');
        availableEventTypes = [];
    }
}

// Загрузить группы для курса (с членством для текущего пользователя)
async function loadGroupsForCourse(courseId) {
    if (!courseId) return [];
    try {
        const userId = window.GamificationAPI.getCurrentUserId();
        const response = await window.GamificationAPI.apiRequest(`/demo/leaderboard/courses/${encodeURIComponent(courseId)}/groups?userId=${encodeURIComponent(userId)}`);
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return await response.json();
    } catch (error) {
        log(`Ошибка загрузки групп: ${error.message}`, 'error');
        return [];
    }
}

// Сохранить последний отправленный eventId
function updateLastEventId(eventId) {
    if (eventId) {
        lastSentEventId = eventId;
        localStorage.setItem(STORAGE_LAST_EVENT_ID, eventId);
    }
}

// Получить сохранённый eventId
function getLastEventId() {
    if (lastSentEventId === null) {
        lastSentEventId = localStorage.getItem(STORAGE_LAST_EVENT_ID);
    }
    return lastSentEventId;
}

// ============================
// Отправка события (общая)
// ============================
async function sendEvent(payload, tokenOverride = null) {
    log(`➡️ Отправка: userId=${payload.userId}, eventType=${payload.eventType}, eventId=${payload.eventId}, course=${payload.courseId || '-'}, group=${payload.groupId || '-'}`, 'info');

    try {
        const overrideHeaders = {};
        if (tokenOverride) {
            overrideHeaders.token = tokenOverride;
        }
        const response = await window.GamificationAPI.apiRequest('/api/v1/event', {
            method: 'POST',
            body: JSON.stringify(payload),
            overrideHeaders: overrideHeaders
        });
        const data = await response.json();

        if (response.ok && data.status === 'success') {
            log(`✅ Успех! Начислено ${data.pointsEarned} XP, всего очков: ${data.totalPoints}. ${data.levelUp ? '🎉 Уровень повышен!' : ''}`, 'success');
            updateLastEventId(payload.eventId);
            return true;
        } else if (data.status === 'duplicate') {
            log(`⚠️ Дубликат события: ${data.message}`, 'warning');
            updateLastEventId(payload.eventId);
            return false;
        } else {
            log(`❌ Ошибка (${response.status}): ${data.message || JSON.stringify(data)}`, 'error');
            return false;
        }
    } catch (error) {
        log(`❌ Ошибка соединения: ${error.message}`, 'error');
        return false;
    }
}

// ============================
// Управление модальным окном
// ============================
let currentModal = null;

function closeModal() {
    if (currentModal && currentModal.remove) {
        currentModal.remove();
        currentModal = null;
    }
}

// Генерация модалки под конкретный сценарий
async function openScenarioModal(scenario) {
    closeModal();

    // Базовые значения
    const currentUserId = window.GamificationAPI.getCurrentUserId();
    let modalConfig = {
        title: '',
        fields: {},
        sendHandler: null
    };

    // Общие данные для всех сценариев, где нужны селекты
    const coursesForSelect = availableCourses.map(c => ({ value: c.courseId, label: `${c.courseId}${c.enrolled ? ' ✓' : ''}` }));
    const eventTypesForSelect = availableEventTypes.map(et => ({ value: et.typeCode, label: `${et.displayName} (${et.points} XP)` }));

    // Сценарий 1: существующий пользователь, курс, группа (выбор из селектов)
    if (scenario === 'existing') {
        modalConfig.title = '📌 Отправка события (существующий пользователь)';
        modalConfig.fields = {
            userId: { type: 'text', label: 'Пользователь', value: currentUserId, disabled: true },
            course: { type: 'select', label: 'Курс', options: coursesForSelect, value: coursesForSelect[0]?.value || '' },
            group: { type: 'select', label: 'Группа (опционально)', options: [], value: '', dependsOnCourse: true },
            eventType: { type: 'select', label: 'Тип события', options: eventTypesForSelect, value: eventTypesForSelect[0]?.value || '' },
            eventId: { type: 'text', label: 'ID события (оставьте пустым для автогенерации)', value: '', disabled: false }
        };
    }
    // Сценарий 2: новый пользователь + новый курс + новая группа (все поля инпуты)
    else if (scenario === 'newUser') {
        modalConfig.title = '✨ Новый пользователь + новый курс + новая группа';
        modalConfig.fields = {
            userId: { type: 'text', label: 'Новый пользователь', value: 'new_student_' + Date.now(), disabled: false },
            course: { type: 'text', label: 'Новый курс', value: 'COURSE_' + Date.now(), disabled: false },
            group: { type: 'text', label: 'Новая группа', value: 'GROUP_' + Date.now(), disabled: false },
            eventType: { type: 'select', label: 'Тип события', options: eventTypesForSelect, value: eventTypesForSelect[0]?.value || '' },
            eventId: { type: 'text', label: 'ID события (оставьте пустым для автогенерации)', value: '', disabled: false }
        };
    }
    // Сценарий 3: новый курс для существующего пользователя
    else if (scenario === 'newCourse') {
        modalConfig.title = '🆕 Новый курс для существующего пользователя';
        modalConfig.fields = {
            userId: { type: 'text', label: 'Пользователь', value: currentUserId, disabled: true },
            course: { type: 'text', label: 'Новый курс', value: 'NEW_COURSE_' + Date.now(), disabled: false },
            group: { type: 'text', label: 'Новая группа (опционально)', value: '', disabled: false },
            eventType: { type: 'select', label: 'Тип события', options: eventTypesForSelect, value: eventTypesForSelect[0]?.value || '' },
            eventId: { type: 'text', label: 'ID события (оставьте пустым для автогенерации)', value: '', disabled: false }
        };
    }
    // Сценарий 4: дубликат eventId
    else if (scenario === 'duplicate') {
        const lastId = getLastEventId();
        if (!lastId) {
            log('❌ Нет сохранённого eventId. Сначала отправьте любое успешное событие.', 'error');
            return;
        }
        modalConfig.title = '🔄 Дубликат eventId (ожидается ошибка)';
        modalConfig.fields = {
            userId: { type: 'text', label: 'Пользователь', value: currentUserId, disabled: true },
            course: { type: 'select', label: 'Курс', options: coursesForSelect, value: coursesForSelect[0]?.value || '' },
            group: { type: 'select', label: 'Группа', options: [], value: '', dependsOnCourse: true },
            eventType: { type: 'select', label: 'Тип события', options: eventTypesForSelect, value: eventTypesForSelect[0]?.value || '' },
            eventId: { type: 'text', label: 'EventId (заблокирован, будет использован сохранённый)', value: lastId, disabled: true }
        };
    }
    // Сценарий 5: пустой eventId
    else if (scenario === 'emptyEventId') {
        modalConfig.title = '⚠️ Пустой eventId (ожидается ошибка валидации)';
        modalConfig.fields = {
            userId: { type: 'text', label: 'Пользователь', value: currentUserId, disabled: true },
            course: { type: 'select', label: 'Курс', options: coursesForSelect, value: coursesForSelect[0]?.value || '' },
            group: { type: 'select', label: 'Группа', options: [], value: '', dependsOnCourse: true },
            eventType: { type: 'select', label: 'Тип события', options: eventTypesForSelect, value: eventTypesForSelect[0]?.value || '' },
            eventId: { type: 'text', label: 'EventId (пустой, заблокирован)', value: '', disabled: true }
        };
    }
    // Сценарий 6: неверный тип события
    else if (scenario === 'invalidType') {
        modalConfig.title = '❌ Неверный тип события (ожидается ошибка)';
        modalConfig.fields = {
            userId: { type: 'text', label: 'Пользователь', value: currentUserId, disabled: true },
            course: { type: 'select', label: 'Курс', options: coursesForSelect, value: coursesForSelect[0]?.value || '' },
            group: { type: 'select', label: 'Группа', options: [], value: '', dependsOnCourse: true },
            eventType: { type: 'text', label: 'Тип события (некорректный)', value: 'invalid_type_xyz', disabled: true },
            eventId: { type: 'text', label: 'ID события', value: 'event_' + Date.now(), disabled: false }
        };
    }
    // Сценарий 7: неверный токен
    else if (scenario === 'invalidToken') {
        modalConfig.title = '🔑 Неверный токен (ожидается 401/403)';
        modalConfig.fields = {
            userId: { type: 'text', label: 'Пользователь', value: currentUserId, disabled: true },
            course: { type: 'select', label: 'Курс', options: coursesForSelect, value: coursesForSelect[0]?.value || '' },
            group: { type: 'select', label: 'Группа', options: [], value: '', dependsOnCourse: true },
            eventType: { type: 'select', label: 'Тип события', options: eventTypesForSelect, value: eventTypesForSelect[0]?.value || '' },
            eventId: { type: 'text', label: 'ID события', value: 'event_' + Date.now(), disabled: false }
        };
    } else {
        return;
    }

    // Создаём DOM модалки
    const modalOverlay = document.createElement('div');
    modalOverlay.className = 'modal-overlay';
    const modalContainer = document.createElement('div');
    modalContainer.className = 'modal-container';

    // Заголовок
    const headerDiv = document.createElement('div');
    headerDiv.className = 'modal-header';
    headerDiv.innerHTML = `<h3>${modalConfig.title}</h3><button class="modal-close">&times;</button>`;
    modalContainer.appendChild(headerDiv);

    // Форма
    const formDiv = document.createElement('div');
    const fieldElements = {};

    for (const [key, cfg] of Object.entries(modalConfig.fields)) {
        const fieldDiv = document.createElement('div');
        fieldDiv.className = 'modal-field';
        const label = document.createElement('label');
        label.textContent = cfg.label;
        fieldDiv.appendChild(label);
        let input;
        if (cfg.type === 'select') {
            input = document.createElement('select');
            cfg.options.forEach(opt => {
                const option = document.createElement('option');
                option.value = opt.value;
                option.textContent = opt.label;
                input.appendChild(option);
            });
            if (cfg.value) input.value = cfg.value;
        } else {
            input = document.createElement('input');
            input.type = 'text';
            input.value = cfg.value || '';
        }
        if (cfg.disabled) input.disabled = true;
        fieldDiv.appendChild(input);
        formDiv.appendChild(fieldDiv);
        fieldElements[key] = input;
    }

    // Если есть dependsOnCourse (группа зависит от курса) — добавляем динамическую загрузку
    let groupSelect = null;
    if (modalConfig.fields.group && modalConfig.fields.group.dependsOnCourse && fieldElements.course && fieldElements.group && fieldElements.course.tagName === 'SELECT') {
        groupSelect = fieldElements.group;
        const courseSelect = fieldElements.course;
        const loadGroups = async () => {
            const courseId = courseSelect.value;
            if (courseId) {
                const groups = await loadGroupsForCourse(courseId);
                groupSelect.innerHTML = '<option value="">-- Все группы --</option>';
                groups.forEach(g => {
                    const opt = document.createElement('option');
                    opt.value = g.groupId;
                    opt.textContent = `${g.groupId}${g.member ? ' ✓' : ''}`;
                    groupSelect.appendChild(opt);
                });
            } else {
                groupSelect.innerHTML = '<option value="">-- Сначала выберите курс --</option>';
            }
        };
        courseSelect.addEventListener('change', loadGroups);
        await loadGroups(); // начальная загрузка, если курс уже выбран
    }

    modalContainer.appendChild(formDiv);

    // Кнопки действий
    const actionsDiv = document.createElement('div');
    actionsDiv.className = 'modal-actions';
    const cancelBtn = document.createElement('button');
    cancelBtn.textContent = 'Отмена';
    cancelBtn.className = 'btn btn-secondary';
    cancelBtn.addEventListener('click', closeModal);
    const sendBtn = document.createElement('button');
    sendBtn.textContent = 'Отправить событие';
    sendBtn.className = 'btn btn-primary';
    actionsDiv.appendChild(cancelBtn);
    actionsDiv.appendChild(sendBtn);
    modalContainer.appendChild(actionsDiv);

    modalOverlay.appendChild(modalContainer);
    document.body.appendChild(modalOverlay);
    currentModal = modalOverlay;

    // Обработчик закрытия
    modalOverlay.querySelector('.modal-close').addEventListener('click', closeModal);
    modalOverlay.addEventListener('click', (e) => {
        if (e.target === modalOverlay) closeModal();
    });

    // Обработчик отправки
    sendBtn.addEventListener('click', async () => {
        // Собираем данные
        let userId = fieldElements.userId.value;
        let courseId = fieldElements.course.value;
        let groupId = fieldElements.group.value;
        let eventType = fieldElements.eventType.value;
        let eventId = fieldElements.eventId.value;

        if (!eventId && !fieldElements.eventId.disabled) {
            eventId = `demo-${Date.now()}`;
            log(`Автоматически сгенерирован eventId: ${eventId}`, 'info');
        }

        if (!userId || !eventType) {
            log('❌ Заполните обязательные поля: пользователь и тип события', 'error');
            return;
        }

        const payload = {
            userId: userId,
            eventType: eventType,
            eventId: eventId
        };
        if (courseId && courseId !== '') payload.courseId = courseId;
        if (groupId && groupId !== '') payload.groupId = groupId;

        let tokenOverride = null;
        if (scenario === 'invalidToken') {
            tokenOverride = 'wrong_token_xyz';
        }

        await sendEvent(payload, tokenOverride);
        closeModal();
    });
}

// ============================
// Инициализация страницы
// ============================
document.addEventListener("DOMContentLoaded", async () => {
    logContainer = document.getElementById('eventLog');
    document.getElementById('clearLogBtn').addEventListener('click', clearLog);

    await loadAllCourses();
    await loadEventTypes();

    // Привязываем сценарии к кнопкам
    document.querySelectorAll('.scenario-btn').forEach(btn => {
        const scenario = btn.getAttribute('data-scenario');
        btn.addEventListener('click', () => openScenarioModal(scenario));
    });

    log('Демо-панель готова. Выберите сценарий для тестирования.', 'success');

    // При смене пользователя в хедере перезагружаем курсы (для селектов)
    document.addEventListener('userChanged', async () => {
        await loadAllCourses();
        log('Пользователь изменён, список курсов обновлён', 'info');
    });
});