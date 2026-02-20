// Анимация прогресс-бара (общая для всех страниц)
document.addEventListener('DOMContentLoaded', () => {
    const bars = document.querySelectorAll('.progress-bar');
    bars.forEach(bar => {
        const target = bar.getAttribute('data-progress');
        if (target) {
            bar.style.width = '0%';
            setTimeout(() => bar.style.width = target, 400);
        }
    });
});