// ============================
// Глобальные переменные состояния
// ============================
let demoRole = localStorage.getItem("demoRole") || "ADMIN";
let demoUserId = localStorage.getItem("demoUserId") || "student001";

let availableCourses = [];            // список курсов пользователя {courseId, groupId?}
let currentCourseId = null;           // выбранный курс
let currentGroupId = null;             // введённая группа (строка)

let availableEventTypes = [];          // активные типы событий с сервера
let lastSentEventId = null;            // для дублирования

// Элементы DOM
let courseSelect, groupInput, eventTypeSelect, eventIdInput, logContainer;

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

// Универсальный fetch с заголовками демо-режима
async function apiCall(url, options = {}) {
    const headers = {
        "Content-Type": "application/json",
        "X-User-Id": demoUserId,
        "X-Role": options.demoRole || demoRole,
        "token": "asdasd2123sadadad21asdasd",
        ...(options.headers || {})
    };
    const response = await fetch(url, { ...options, headers });
    return response;
}

// Загрузка курсов пользователя (через эндпоинт /api/v1/leaderboard/users/{userId}/courses)
async function loadUserCourses() {
    try {
        const response = await apiCall(`/api/v1/leaderboard/users/${demoUserId}/courses`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        const data = await response.json();
        availableCourses = data.courses || [];
        log(`Загружено курсов: ${availableCourses.length}`, 'success');

        // Заполнить выпадающий список курсов
        courseSelect.innerHTML = '<option value="">-- Выберите курс --</option>';
        availableCourses.forEach(course => {
            const option = document.createElement('option');
            option.value = course.courseId;
            option.textContent = `${course.courseId} (${course.totalPointsInCourse || 0} очков)`;
            courseSelect.appendChild(option);
        });

        // Если был выбран курс ранее – попробовать восстановить (по localStorage)
        const savedCourse = localStorage.getItem('demoCourseId');
        if (savedCourse && availableCourses.some(c => c.courseId === savedCourse)) {
            courseSelect.value = savedCourse;
        } else if (availableCourses.length > 0) {
            courseSelect.value = availableCourses[0].courseId;
        }
        onCourseChange(); // обновить текущий курс
    } catch (error) {
        log(`Ошибка загрузки курсов: ${error.message}`, 'error');
        courseSelect.innerHTML = '<option value="">-- Ошибка загрузки --</option>';
    }
}

// Загрузка активных типов событий (админский эндпоинт)
async function loadEventTypes() {
    try {
        const response = await apiCall(`/demo/admin/event-types/all-types?page=0&size=100`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
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
function onCourseChange() {
    currentCourseId = courseSelect.value;
    if (currentCourseId) {
        localStorage.setItem('demoCourseId', currentCourseId);
        log(`Выбран курс: ${currentCourseId}`, 'info');
    } else {
        localStorage.removeItem('demoCourseId');
    }
}

// Отправка события (основная)
async function sendEvent(eventType, eventId) {
    if (!eventType || eventType.trim() === '') {
        log('❌ Не указан тип события', 'error');
        return;
    }

    let payload = {
        userId: demoUserId,
        eventType: eventType,
        eventId: eventId
    };

    if(!!currentCourseId) {
        payload = {
            ...payload,
            courseId: currentCourseId
        }
    }

    if(!!currentGroupId) {
        payload = {
            ...payload,
            groupId: currentGroupId
        }
    }

    log(`➡️ Отправка события: ${eventType} / ${eventId}`, 'info');
    try {
        const response = await apiCall('/api/v1/event', {
            method: 'POST',
            body: JSON.stringify(payload)
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
        role: demoRole,
        userId: demoUserId,
        courseId: currentCourseId || (availableCourses.length ? availableCourses[0].courseId : 'DEMO_COURSE'),
        groupId: groupInput.value.trim() || null
    };
}

function goToProfile() {
    const { userId, courseId, groupId } = getDemoState();
    if (!courseId) {
        log('❌ Не выбран курс. Выберите курс из списка.', 'error');
        return;
    }
    const url = `/demo/users/${userId}?courseId=${encodeURIComponent(courseId)}&groupId=${groupId || ''}`;
    window.location.href = url;
}

function goToLeaderboard() {
    const { role, userId, courseId, groupId } = getDemoState();
    if (!courseId) {
        log('❌ Не выбран курс. Выберите курс из списка.', 'error');
        return;
    }
    let url;
    if (role === 'STUDENT') {
        url = `/demo/leaderboard/course/${encodeURIComponent(courseId)}/user/${encodeURIComponent(userId)}?groupId=${groupId || ''}`;
    } else {
        url = `/demo/leaderboard/course/${encodeURIComponent(courseId)}/user/${encodeURIComponent(userId)}?groupId=${groupId || ''}&size=20`;
    }
    window.location.href = url;
}

// ============================
// ФУНКЦИИ для загрузки списка пользователей и ручного ввода
// ============================
async function loadUsersList() {
    const userSelect = document.getElementById('demoUser');
    if (!userSelect) return;

    try {
        const response = await apiCall(`/api/admin/users?page=0&size=1000`, {demoRole: 'ADMIN'});
        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        const pageData = await response.json();
        const users = pageData.content || [];

        const currentUserId = demoUserId;

        userSelect.innerHTML = '';
        users.forEach(user => {
            const option = document.createElement('option');
            option.value = user.userId;
            option.textContent = `${user.userId} (${user.totalPoints} XP, lvl ${user.level})`;
            if (user.userId === currentUserId) {
                option.selected = true;
            }
            userSelect.appendChild(option);
        });

        if (currentUserId && !users.some(u => u.userId === currentUserId)) {
            const option = document.createElement('option');
            option.value = currentUserId;
            option.textContent = `${currentUserId} (вне списка)`;
            option.selected = true;
            userSelect.appendChild(option);
        }

        log(`Загружено пользователей: ${users.length}`, 'success');
    } catch (error) {
        console.error('Ошибка загрузки списка пользователей:', error);
        log(`Не удалось загрузить список пользователей: ${error.message}`, 'error');
        userSelect.innerHTML = '<option value="">-- нет данных --</option>';
    }
}

function onUserSelectChange() {
    const userSelect = document.getElementById('demoUser');
    if (!userSelect) return;
    const newUserId = userSelect.value;
    if (!newUserId) return;

    demoUserId = newUserId;
    localStorage.setItem("demoUserId", demoUserId);
    document.getElementById("manualUserId").value = demoUserId;
    log(`Выбран пользователь → ${demoUserId}`, 'info');
    document.dispatchEvent(new Event("userChanged"));

    loadUserCourses().catch(e => log(`Ошибка загрузки курсов: ${e.message}`, 'error'));
    if (window.GamificationWidgetConfig) {
        window.GamificationWidgetConfig.userId = demoUserId;
        window.GamificationWidgetConfig.headers["X-User-Id"] = demoUserId;
        if (window.initGamificationWidget) window.initGamificationWidget(window.GamificationWidgetConfig);
    }
}

function setManualUserId() {
    const manualInput = document.getElementById('manualUserId');
    let newUserId = manualInput.value.trim();
    if (!newUserId) {
        log('❌ Введите непустой UserId', 'error');
        return;
    }

    demoUserId = newUserId;
    localStorage.setItem("demoUserId", demoUserId);
    log(`Установлен пользователь (ручной ввод) → ${demoUserId}`, 'info');

    const userSelect = document.getElementById('demoUser');
    if (userSelect) {
        let optionExists = false;
        for (let i = 0; i < userSelect.options.length; i++) {
            if (userSelect.options[i].value === newUserId) {
                userSelect.selectedIndex = i;
                optionExists = true;
                break;
            }
        }
        if (!optionExists && userSelect.options.length > 0) {
            const opt = document.createElement('option');
            opt.value = newUserId;
            opt.textContent = `${newUserId} (вручную)`;
            opt.selected = true;
            userSelect.appendChild(opt);
        }
    }

    document.dispatchEvent(new Event("userChanged"));
    loadUserCourses().catch(e => log(`Ошибка загрузки курсов: ${e.message}`, 'error'));
    if (window.GamificationWidgetConfig) {
        window.GamificationWidgetConfig.userId = demoUserId;
        window.GamificationWidgetConfig.headers["X-User-Id"] = demoUserId;
        if (window.initGamificationWidget) window.initGamificationWidget(window.GamificationWidgetConfig);
    }
}

// ============================
// Инициализация и обработчики событий DOM
// ============================
document.addEventListener("DOMContentLoaded", async () => {
    // Найти элементы
    courseSelect = document.getElementById('courseSelect');
    groupInput = document.getElementById('groupIdInput');
    eventTypeSelect = document.getElementById('eventTypeSelect');
    eventIdInput = document.getElementById('eventIdInput');
    logContainer = document.getElementById('eventLog');

    // Кнопки
    document.getElementById('sendEventBtn').addEventListener('click', sendFromUI);
    document.getElementById('sendDuplicateBtn').addEventListener('click', sendDuplicate);
    document.getElementById('sendInvalidTypeBtn').addEventListener('click', sendInvalidType);
    document.getElementById('sendEmptyEventIdBtn').addEventListener('click', sendEmptyEventId);
    document.getElementById('clearLogBtn').addEventListener('click', clearLog);

    // Переключение роли/пользователя из header – расширяем существующие
    const roleSelect = document.getElementById("demoRole");
    const userSelect = document.getElementById("demoUser");

    if (roleSelect) {
        roleSelect.value = demoRole;
        roleSelect.addEventListener("change", async (e) => {
            demoRole = e.target.value;
            localStorage.setItem("demoRole", demoRole);
            log(`Переключение роли → ${demoRole}`, 'info');
            await loadUsersList(); // перезагружаем список пользователей (скрыть/показать)
            document.dispatchEvent(new Event("roleChanged"));
        });
    }

    if (userSelect) {
        // Загрузим список пользователей и установим начальное значение
        await loadUsersList();
        // Если после загрузки значение не совпадает с текущим demoUserId – синхронизируем
        if (userSelect.value !== demoUserId && userSelect.options.length > 0) {
            // пытаемся найти опцию с нужным значением
            let found = false;
            for (let i = 0; i < userSelect.options.length; i++) {
                if (userSelect.options[i].value === demoUserId) {
                    userSelect.selectedIndex = i;
                    found = true;
                    break;
                }
            }
            if (!found && demoUserId) {
                const opt = document.createElement('option');
                opt.value = demoUserId;
                opt.textContent = `${demoUserId} (вне списка)`;
                opt.selected = true;
                userSelect.appendChild(opt);
            }
        } else if (userSelect.options.length === 0) {
            // если нет опций, добавим текущего пользователя как заглушку
            const opt = document.createElement('option');
            opt.value = demoUserId;
            opt.textContent = demoUserId;
            opt.selected = true;
            userSelect.appendChild(opt);
        }

        userSelect.addEventListener("change", onUserSelectChange);
    }

    // Ручной ввод
    const setUserIdBtn = document.getElementById("setUserIdBtn");
    const manualInput = document.getElementById("manualUserId");
    if (setUserIdBtn) {
        setUserIdBtn.addEventListener("click", setManualUserId);
    }
    if (manualInput) {
        manualInput.value = demoUserId;
        manualInput.addEventListener("keypress", (e) => {
            if (e.key === "Enter") setManualUserId();
        });
    }

    // При смене курса обновляем currentCourseId
    if (courseSelect) courseSelect.addEventListener('change', onCourseChange);

    // Загрузить данные (курсы, типы событий)
    await loadUserCourses();
    await loadEventTypes();

    // Восстановить группу из localStorage, если есть
    const savedGroup = localStorage.getItem('demoGroupId');
    if (savedGroup && groupInput) groupInput.value = savedGroup;
    if (groupInput) {
        groupInput.addEventListener('change', () => {
            localStorage.setItem('demoGroupId', groupInput.value);
            currentGroupId = groupInput.value;
        });
    }

    // Значение eventId по умолчанию – пусто (будет auto генерироваться)
    if (eventIdInput) eventIdInput.placeholder = "auto";
    log('Демо-панель готова. Выберите курс и тип события.', 'success');
});

// Экспорт функций для глобального доступа (некоторые вызываются из onclick в HTML)
window.createEventType = async function() {
    await apiCall('/api/admin/event-types', {
        method: 'POST',
        body: JSON.stringify({
            typeCode: "demo_type_" + Date.now(),
            displayName: "Demo тип (авто)",
            points: 70
        })
    }).then(res => {
        if (res.ok) {
            log('✅ Создан новый тип события (демо)', 'success');
            loadEventTypes(); // обновить список
        } else {
            log('❌ Ошибка при создании типа', 'error');
        }
    }).catch(e => log(`Ошибка: ${e.message}`, 'error'));
};

window.goToProfile = goToProfile;
window.goToLeaderboard = goToLeaderboard;

// Обработка старых функций из demo.html (кнопки Quiz/Homework) – перенаправим на sendFromUI с соответствующим типом
window.sendQuiz = () => {
    const quizType = availableEventTypes.find(t => t.typeCode === 'quiz');
    if (quizType) {
        eventTypeSelect.value = 'quiz';
        sendFromUI();
    } else {
        log('Тип события "quiz" не найден в списке. Сначала создайте его через админку.', 'error');
    }
};
window.sendHomework = () => {
    const hwType = availableEventTypes.find(t => t.typeCode === 'homework');
    if (hwType) {
        eventTypeSelect.value = 'homework';
        sendFromUI();
    } else {
        log('Тип события "homework" не найден. Создайте его в админке.', 'error');
    }
};