let demoRole = localStorage.getItem("demoRole") || "ADMIN";
let demoUserId = localStorage.getItem("demoUserId") || "student001";

document.addEventListener("DOMContentLoaded", () => {
    const roleSelect = document.getElementById("demoRole");
    const userSelect = document.getElementById("demoUser");

    if (roleSelect) roleSelect.value = demoRole;
    if (userSelect) userSelect.value = demoUserId;

    roleSelect?.addEventListener("change", e => {
        demoRole = e.target.value;
        localStorage.setItem("demoRole", demoRole);
        logEvent(`Переключение роли → ${demoRole}`);
    });

    userSelect?.addEventListener("change", e => {
        demoUserId = e.target.value;
        localStorage.setItem("demoUserId", demoUserId);
        logEvent(`Выбран пользователь → ${demoUserId}`);
    });
});

function api(url, options = {}) {
    return fetch(url, {
        ...options, headers: {
            "Content-Type": "application/json",
            "X-User-Id": demoUserId,
            "X-Role": demoRole, ...(options.headers || {})
        }
    });
}

function sendEvent(type) {
    fetch('/api/v1/event', {
        method: 'POST',
        headers: {
            "Content-Type": "application/json",
            token: '33dsfasdffs2123sfdfsdfsdf'
        },
        body: JSON.stringify({
            userId: demoUserId, eventType: type, eventId: "demo-" + Date.now()
        })
    }).then(() => alert("Событие отправлено"))
        .then(() => logEvent(`LMS → событие "${type}" для ${demoUserId}`));
}

function sendQuiz() {
    sendEvent('quiz');
}

function sendHomework() {
    sendEvent('homework');
}

function createEventType() {
    api('/api/admin/event-types', {
        method: 'POST', body: JSON.stringify({
            typeCode: "demo_type", displayName: "Demo тип", points: 70
        })
    }).then(() => alert("Создано"));
}

function logEvent(message) {
    const log = document.getElementById("eventLog");
    if (!log) return;

    const time = new Date().toLocaleTimeString();

    const entry = document.createElement("div");
    entry.className = "event-log-entry";
    entry.innerHTML = `<span>[${time}]</span> ${message}`;

    log.prepend(entry);
}

function getDemoState() {
    return {
        role: document.getElementById('demoRole').value,
        userId: document.getElementById('demoUser').value,

        // можно захардкодить для демо
        courseId: 'MATH-101',
        groupId: 'M-21-2'
    };
}

function goToProfile() {
    const {userId, courseId, groupId} = getDemoState();

    const url = `/demo/users/${userId}?courseId=${courseId}&groupId=${groupId}`;
    window.location.href = url;
}

function goToLeaderboard() {
    const {role, userId, courseId, groupId} = getDemoState();

    let url;

    if (role === 'STUDENT') {
        // персонализированный лидерборд
        url = `/demo/leaderboard/course/${courseId}/user/${userId}?groupId=${groupId}`;
    } else {
        // админ / преподаватель → можно показать любого пользователя
        url = `/demo/leaderboard/course/${courseId}/user/${userId}?groupId=${groupId}&size=20`;
    }

    window.location.href = url;
}