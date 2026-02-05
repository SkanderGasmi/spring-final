// ModalManager.js
export class ModalManager {
    constructor() {
        this.modals = new Map();
        this.currentModal = null;
        this.eventListeners = new Map();
        this.init();
    }

    init() {
        // Setup global close handlers
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.currentModal) {
                this.close();
            }
        });

        document.getElementById('modal')?.addEventListener('click', (e) => {
            if (e.target.id === 'modal') {
                this.close();
            }
        });

        document.getElementById('closeModal')?.addEventListener('click', () => {
            this.close();
        });
    }

    register(name, component) {
        this.modals.set(name, component);
        console.log(`Modal registered: ${name}`);
    }

    async open(name, data = {}) {
        if (this.currentModal === name) return;

        await this.closeCurrent();

        const component = this.modals.get(name);
        if (!component) {
            console.error(`Modal "${name}" not registered`);
            return;
        }

        const modalElement = document.getElementById('modal');
        const modalBody = document.getElementById('modal-body');

        if (!modalElement || !modalBody) {
            console.error('Modal elements not found in DOM');
            return;
        }

        // Clean previous content
        modalBody.innerHTML = '';

        // Render new modal
        component.render(modalBody, data);

        // Store cleanup function if provided
        if (component.cleanup) {
            this.eventListeners.set(name, component.cleanup);
        }

        // Show modal
        modalElement.style.display = 'block';
        modalElement.setAttribute('aria-hidden', 'false');
        document.body.style.overflow = 'hidden'; // Prevent scrolling

        this.currentModal = name;

        // Focus first input
        setTimeout(() => {
            const firstInput = modalBody.querySelector('input, button, [tabindex]');
            firstInput?.focus();
        }, 100);
    }

    async closeCurrent() {
        if (!this.currentModal) return;

        // Run cleanup
        const cleanup = this.eventListeners.get(this.currentModal);
        if (cleanup) {
            cleanup();
            this.eventListeners.delete(this.currentModal);
        }

        const modalElement = document.getElementById('modal');
        if (modalElement) {
            modalElement.style.display = 'none';
            modalElement.setAttribute('aria-hidden', 'true');
            document.body.style.overflow = '';
        }

        this.currentModal = null;
    }

    close() {
        return this.closeCurrent();
    }

    getCurrentModal() {
        return this.currentModal;
    }

    isOpen() {
        return this.currentModal !== null;
    }
}

// Singleton instance
export const modalManager = new ModalManager();