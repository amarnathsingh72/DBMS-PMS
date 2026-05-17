// PlacementPro Global Interactions
const Toast = {
    show(message, type = 'success') {
        const toast = document.createElement('div');
        toast.className = `fixed bottom-6 right-6 px-6 py-3 rounded-xl shadow-2xl transform transition-all duration-300 translate-y-20 opacity-0 z-50 flex items-center gap-3`;
        
        const colors = {
            success: 'bg-emerald-500 text-white',
            error: 'bg-red-500 text-white',
            info: 'bg-indigo-600 text-white'
        };
        
        const icons = {
            success: '✅',
            error: '❌',
            info: 'ℹ️'
        };
        
        toast.classList.add(...colors[type].split(' '));
        toast.innerHTML = `<span>${icons[type]}</span><span class="font-bold">${message}</span>`;
        
        document.body.appendChild(toast);
        
        // Animate in
        setTimeout(() => {
            toast.classList.remove('translate-y-20', 'opacity-0');
        }, 10);
        
        // Animate out
        setTimeout(() => {
            toast.classList.add('translate-y-20', 'opacity-0');
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }
};

// Tooltip helper
function initTooltips() {
    // Simple logic to show tooltips if needed
}

document.addEventListener('DOMContentLoaded', () => {
    console.log('PlacementPro UI Initialized');
});
