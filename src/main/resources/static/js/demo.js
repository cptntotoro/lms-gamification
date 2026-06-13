// ============================
// Глобальные переменные состояния
// ============================

let availableCourses = [];            // список всех курсов {courseId, enrolled, totalPointsInCourse}
let availableGroups = [];             // список групп выбранного курса {groupId, member}
let currentCourseId = null;           // выбранный курс
let currentGroupId = null;            // выбранная группа (строка)

let availableEventTypes = [];          // активные типы событий с сервера
let lastSentEventId = null;            // для дублирования

// Элементы DOM
let courseSelect, groupSelect, eventTypeSelect, eventIdInput, logContainer, roleSelect;

// ============================
// Вспомогательные функции
// ============================
function log(message, type = 'info') {
    if (!logContainer) return;
    const time = new Date().toLocaleTimeString();
    const entry = document.createElement('div');
    entry.className = `log-item log-${type}`;
    entry.innerHTML = `<span>[${time}]</span> ${message}`;
    logContainer.prepend(entry);
    // Ограничим длину лога 200 записями
    while (logContainer.children.length > 200) {
        logContainer.removeChild(logContainer.lastChild);
    }
}

function clearLog() {
    if (logContainer) logContainer.innerHTML = '';
    log('Лог очищен', 'info');
}

// Загрузка ВСЕХ курсов системы с признаком записи пользователя
async function loadAllCourses() {
    try {
        const userId = window.GamificationAPI.getCurrentUserId();
        const response = await window.GamificationAPI.apiRequest(`/api/v1/leaderboard/courses/all?userId=${encodeURIComponent(userId)}`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        const data = await response.json();
        availableCourses = data;
        log(`Загружено курсов: ${availableCourses.length}`, 'success');

        // Заполнить выпадающий список курсов
        courseSelect.innerHTML = '<option value="">-- Выберите курс --</option>';
        availableCourses.forEach(course => {
            const option = document.createElement('option');
            option.value = course.courseId;
            let text = `${course.courseId}${course.displayName ? ' (' + course.displayName + ')' : ''}`;
            if (course.enrolled) {
                text += ` ✓ (${course.totalPointsInCourse || 0} очков)`;
                option.style.fontWeight = 'bold';
                option.style.backgroundColor = '#e6f7e6';
            } else {
                text += ` (не записан)`;
            }
            option.textContent = text;
            courseSelect.appendChild(option);
        });
        // Добавляем опцию добавления нового курса
        const addCourseOption = document.createElement('option');
        addCourseOption.value = '__ADD_NEW_COURSE__';
        addCourseOption.textContent = '➕ Добавить новый курс';
        addCourseOption.style.fontStyle = 'italic';
        addCourseOption.style.color = '#2563eb';
        courseSelect.appendChild(addCourseOption);

        // Восстановить сохранённый курс из localStorage
        const savedCourse = localStorage.getItem('demoCourseId');
        if (savedCourse && availableCourses.some(c => c.courseId === savedCourse)) {
            courseSelect.value = savedCourse;
        } else if (availableCourses.length > 0) {
            // предвыбираем первый записанный курс, если есть
            const firstEnrolled = availableCourses.find(c => c.enrolled);
            courseSelect.value = firstEnrolled ? firstEnrolled.courseId : availableCourses[0].courseId;
        }
        await onCourseChange(); // загрузит группы для выбранного курса
    } catch (error) {
        log(`Ошибка загрузки курсов: ${error.message}`, 'error');
        courseSelect.innerHTML = '<option value="">-- Ошибка загрузки --</option>';
    }
}

// Загрузка групп для выбранного курса с членством пользователя
async function loadGroupsForCourse(courseId) {
    if (!courseId) return [];
    try {
        const userId = window.GamificationAPI.getCurrentUserId();
        const response = await window.GamificationAPI.apiRequest(`/api/v1/leaderboard/courses/${encodeURIComponent(courseId)}/groups?userId=${encodeURIComponent(userId)}`);
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const data = await response.json();
        return data;
    } catch (error) {
        log(`Ошибка загрузки групп: ${error.message}`, 'error');
        return [];
    }
}

// Заполнить селектор групп
function populateGroupSelect(groups, preselectedGroupId) {
    groupSelect.innerHTML = '<option value="">-- Все группы --</option>';
    if (groups.length === 0) {
        groupSelect.innerHTML = '<option value="">-- Нет групп --</option>';
    } else {
        groups.forEach(group => {
            const option = document.createElement('option');
            option.value = group.groupId;
            let text = `${group.groupId}${group.displayName ? ' (' + group.displayName + ')' : ''}`;
            if (group.member) {
                text += ` ✓`;
                option.style.fontWeight = 'bold';
                option.style.backgroundColor = '#e6f7e6';
            }
            option.textContent = text;
            groupSelect.appendChild(option);
        });
    }
    // Добавляем опцию добавления новой группы
    const addGroupOption = document.createElement('option');
    addGroupOption.value = '__ADD_NEW_GROUP__';
    addGroupOption.textContent = '➕ Добавить новую группу';
    addGroupOption.style.fontStyle = 'italic';
    addGroupOption.style.color = '#2563eb';
    groupSelect.appendChild(addGroupOption);

    if (preselectedGroupId && groups.some(g => g.groupId === preselectedGroupId)) {
        groupSelect.value = preselectedGroupId;
    } else {
        groupSelect.value = '';
    }
}

// Загрузка типов событий
async function loadEventTypes() {
    try {
        const response = await window.GamificationAPI.apiRequest(`/demo/admin/event-types/all-types?page=0&size=100`);
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const page = await response.json();
        availableEventTypes = page.content.filter(t => t.active);
        log(`Загружено типов событий: ${availableEventTypes.length}`, 'success');

        eventTypeSelect.innerHTML = '<option value="">-- Выберите тип --</option>';
        availableEventTypes.forEach(et => {
            const option = document.createElement('option');
            option.value = et.typeCode;
            option.textContent = `${et.displayName} (${et.points} XP)`;
            eventTypeSelect.appendChild(option);
        });
    } catch (error) {
        log(`Ошибка загрузки типов событий: ${error.message}`, 'error');
        eventTypeSelect.innerHTML = '<option value="">-- Ошибка --</option>';
    }
}

// Обработка смены курса
async function onCourseChange() {
    const newCourseId = courseSelect.value;
    if (newCourseId === '__ADD_NEW_COURSE__') {
        const newCourseInput = prompt('Введите идентификатор нового курса:');
        if (newCourseInput && newCourseInput.trim()) {
            // Добавляем новый курс в селектор временно
            const newOption = document.createElement('option');
            newOption.value = newCourseInput.trim();
            newOption.textContent = `${newCourseInput.trim()} (новый)`;
            newOption.style.fontWeight = 'bold';
            newOption.style.backgroundColor = '#ffffcc';
            const addOption = courseSelect.querySelector('option[value="__ADD_NEW_COURSE__"]');
            courseSelect.insertBefore(newOption, addOption);
            courseSelect.value = newCourseInput.trim();
            currentCourseId = newCourseInput.trim();
            // Загружаем группы (скорее всего пусто)
            availableGroups = await loadGroupsForCourse(currentCourseId);
            populateGroupSelect(availableGroups, null);
        } else {
            // восстанавливаем предыдущее значение
            const savedCourse = localStorage.getItem('demoCourseId');
            if (savedCourse && availableCourses.some(c => c.courseId === savedCourse)) {
                courseSelect.value = savedCourse;
            } else if (availableCourses.length > 0) {
                courseSelect.value = availableCourses[0].courseId;
            } else {
                courseSelect.value = '';
            }
            currentCourseId = courseSelect.value;
            if (currentCourseId) {
                availableGroups = await loadGroupsForCourse(currentCourseId);
                populateGroupSelect(availableGroups, null);
            }
        }
    } else {
        currentCourseId = newCourseId;
        if (currentCourseId) {
            localStorage.setItem('demoCourseId', currentCourseId);
            log(`Выбран курс: ${currentCourseId}`, 'info');
            // Загружаем группы для этого курса
            availableGroups = await loadGroupsForCourse(currentCourseId);
            const savedGroup = localStorage.getItem('demoGroupId');
            populateGroupSelect(availableGroups, savedGroup);
        } else {
            localStorage.removeItem('demoCourseId');
            groupSelect.innerHTML = '<option value="">-- Все группы --</option>';
        }
    }
}

// Отправка события (основная)
async function sendEvent(eventType, eventId) {
    if (!eventType || eventType.trim() === '') {
        log('❌ Не указан тип события', 'error');
        return;
    }

    let payload = {
        userId: window.GamificationAPI.getCurrentUserId(),
        eventType: eventType,
        eventId: eventId
    };
    if (currentCourseId) payload.courseId = currentCourseId;
    if (currentGroupId && currentGroupId !== '__ADD_NEW_GROUP__') payload.groupId = currentGroupId;

    log(`➡️ Отправка события: ${eventType} / ${eventId}`, 'info');
    try {
        const response = await window.GamificationAPI.apiRequest('/api/v1/event', {
            method: 'POST',
            body: JSON.stringify(payload),
            role: roleSelect.value
        });
        const data = await response.json();

        if (response.ok && data.status === 'success') {
            log(`✅ Успех! Начислено ${data.pointsEarned} XP, всего очков: ${data.totalPoints}. ${data.levelUp ? '🎉 Уровень повышен!' : ''}`, 'success');
        } else if (data.status === 'duplicate') {
            log(`⚠️ Дубликат события: ${data.message}`, 'warning');
        } else {
            log(`❌ Ошибка сервера (${response.status}): ${data.message || JSON.stringify(data)}`, 'error');
        }
        lastSentEventId = eventId;
        // Виджет обновится сам через setInterval, но можно принудительно вызвать refresh, если нужно
    } catch (error) {
        log(`❌ Ошибка соединения: ${error.message}`, 'error');
    }
}

// Отправить событие с выбранными в UI типом и eventId
function sendFromUI() {
    const eventType = eventTypeSelect.value;
    let eventId = eventIdInput.value.trim();
    if (eventId === '' || eventId === 'auto') {
        eventId = `demo-${Date.now()}`;
        eventIdInput.value = eventId;
        log(`Автоматически сгенерирован eventId: ${eventId}`, 'info');
    }
    sendEvent(eventType, eventId);
}

// Отправить дубликат последнего eventId
function sendDuplicate() {
    if (!lastSentEventId) {
        log('❌ Нет предыдущего отправленного события. Сначала отправьте что-нибудь.', 'error');
        return;
    }
    const eventType = eventTypeSelect.value;
    if (!eventType) {
        log('❌ Выберите тип события', 'error');
        return;
    }
    sendEvent(eventType, lastSentEventId);
}

// Отправить заведомо неверный тип события
function sendInvalidType() {
    const fakeType = 'nonexistent_type_' + Date.now();
    const eventId = `invalid-${Date.now()}`;
    sendEvent(fakeType, eventId);
}

// Отправить с пустым eventId (проверка валидации на бэкенде)
function sendEmptyEventId() {
    const eventType = eventTypeSelect.value;
    if (!eventType) {
        log('❌ Выберите тип события', 'error');
        return;
    }
    sendEvent(eventType, '');
}

// ============================
// Навигация (Profile / Leaderboard) с учётом выбранных курса/группы
// ============================
function getDemoState() {
    return {
        role: window.GamificationAPI.getCurrentRole(),
        userId: window.GamificationAPI.getCurrentUserId(),
        courseId: currentCourseId || (availableCourses.length ? availableCourses.find(c => c.enrolled)?.courseId || availableCourses[0].courseId : 'DEMO_COURSE'),
        groupId: groupSelect.value === '__ADD_NEW_GROUP__' ? '' : (groupSelect.value || null)
    };
}

// Обработчик смены роли (только на demo-странице)
function onRoleSelectChange() {
    const newRole = roleSelect.value;
    window.GamificationAPI.setCurrentRole(newRole);
    log(`Роль изменена на ${newRole}`, 'info');
}

document.addEventListener("DOMContentLoaded", async () => {
    // Найти элементы
    courseSelect = document.getElementById('courseSelect');
    groupSelect = document.getElementById('groupIdSelect');
    eventTypeSelect = document.getElementById('eventTypeSelect');
    eventIdInput = document.getElementById('eventIdInput');
    logContainer = document.getElementById('eventLog');
    roleSelect = document.getElementById('demoRoleSelect');

    // Кнопки
    document.getElementById('sendEventBtn').addEventListener('click', sendFromUI);
    document.getElementById('sendDuplicateBtn').addEventListener('click', sendDuplicate);
    document.getElementById('sendInvalidTypeBtn').addEventListener('click', sendInvalidType);
    document.getElementById('sendEmptyEventIdBtn').addEventListener('click', sendEmptyEventId);
    document.getElementById('clearLogBtn').addEventListener('click', clearLog);

    if (courseSelect) courseSelect.addEventListener('change', onCourseChange);
    if (groupSelect) groupSelect.addEventListener('change', () => {
        if (groupSelect.value === '__ADD_NEW_GROUP__') {
            // Проверка выбора группы
            let selectedGroup = groupSelect.value;
            const newGroup = prompt('Введите идентификатор новой группы:');
            if (newGroup && newGroup.trim()) {
                selectedGroup = newGroup.trim();
                // Добавляем временно в селектор
                const newOption = document.createElement('option');
                newOption.value = selectedGroup;
                newOption.textContent = `${selectedGroup} (новая)`;
                newOption.style.fontWeight = 'bold';
                newOption.style.backgroundColor = '#ffffcc';
                const addOption = groupSelect.querySelector('option[value="__ADD_NEW_GROUP__"]');
                groupSelect.insertBefore(newOption, addOption);
                groupSelect.value = selectedGroup;
            } else {
                selectedGroup = '';
                groupSelect.value = '';
            }
        } else {
            currentGroupId = groupSelect.value;
            if (currentGroupId) localStorage.setItem('demoGroupId', currentGroupId);
            else localStorage.removeItem('demoGroupId');
        }
    });

    // Инициализация выбора роли
    if (roleSelect) {
        roleSelect.value = window.GamificationAPI.getCurrentRole();
        roleSelect.addEventListener('change', onRoleSelectChange);
    }

    await loadAllCourses();
    await loadEventTypes();

    // Восстановить группу из localStorage, если есть (после загрузки групп)
    const savedGroup = localStorage.getItem('demoGroupId');
    if (savedGroup && groupSelect) {
        // Отложим установку, т.к. группы могут быть ещё не загружены
        setTimeout(() => {
            if (groupSelect.querySelector(`option[value="${savedGroup}"]`)) {
                groupSelect.value = savedGroup;
                currentGroupId = savedGroup;
            }
        }, 100);
    }

    // Значение eventId по умолчанию – пусто (будет auto генерироваться)
    if (eventIdInput) eventIdInput.placeholder = "auto";
    log('Демо-панель готова. Выберите курс и тип события.', 'success');

    // При смене пользователя (из header.js) перезагружаем курсы
    document.addEventListener('userChanged', () => {
        loadAllCourses().catch(e => log(`Ошибка загрузки курсов: ${e.message}`, 'error'));
    });
});

window.goToProfile = goToProfile;
window.goToLeaderboard = goToLeaderboard;