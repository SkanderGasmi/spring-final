// forms/BaseForm.js
export class BaseForm {
    constructor(config) {
        this.config = config;
        this.inputs = {};
        this.errors = {};
        this.isSubmitting = false;
    }

    render(container) {
        container.innerHTML = this.generateTemplate();
        this.cacheElements();
        this.bindEvents();
        this.setupValidation();
    }

    generateTemplate() {
        const { title, fields, submitText } = this.config;

        const fieldsHTML = fields.map(field => this.generateFieldHTML(field)).join('');

        return `
            <h2>${title}</h2>
            ${fieldsHTML}
            <button class="dashboard-btn" id="${this.config.submitId}" 
                    ${this.isSubmitting ? 'disabled' : ''}>
                ${this.isSubmitting ? 'Processing...' : submitText}
            </button>
            <div id="${this.config.errorId}" class="error-message" role="alert"></div>
        `;
    }

    generateFieldHTML(field) {
        const { id, type, label, placeholder, required, validation } = field;

        return `
            <div class="form-group">
                <label for="${id}" class="form-label">
                    ${label} ${required ? '<span class="required">*</span>' : ''}
                </label>
                <input type="${type}" 
                       id="${id}" 
                       name="${id}"
                       placeholder="${placeholder}"
                       class="input-field ${this.errors[id] ? 'error' : ''}"
                       ${required ? 'required aria-required="true"' : ''}
                       ${validation ? `data-validation="${validation}"` : ''}>
                <div class="field-error" id="${id}-error"></div>
            </div>
        `;
    }

    cacheElements() {
        this.config.fields.forEach(field => {
            this.inputs[field.id] = document.getElementById(field.id);
        });
        this.submitBtn = document.getElementById(this.config.submitId);
        this.errorContainer = document.getElementById(this.config.errorId);
    }

    bindEvents() {
        this.submitBtn.addEventListener('click', () => this.handleSubmit());

        // Enter key support
        Object.values(this.inputs).forEach(input => {
            input.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') this.handleSubmit();
            });
        });
    }

    async handleSubmit() {
        if (this.isSubmitting) return;

        if (!this.validateAll()) {
            this.showFormError('Please fix the errors above.');
            return;
        }

        this.setSubmitting(true);

        try {
            const formData = this.getFormData();
            await this.config.onSubmit(formData);
        } catch (error) {
            this.handleError(error);
        } finally {
            this.setSubmitting(false);
        }
    }

    validateAll() {
        let isValid = true;

        this.config.fields.forEach(field => {
            if (field.required && !this.inputs[field.id].value.trim()) {
                this.showFieldError(field.id, `${field.label} is required`);
                isValid = false;
            }
        });

        return isValid;
    }

    showFieldError(fieldId, message) {
        const errorEl = document.getElementById(`${fieldId}-error`);
        if (errorEl) {
            errorEl.textContent = message;
            this.inputs[fieldId].classList.add('error');
        }
    }

    clearFieldError(fieldId) {
        const errorEl = document.getElementById(`${fieldId}-error`);
        if (errorEl) {
            errorEl.textContent = '';
            this.inputs[fieldId].classList.remove('error');
        }
    }

    setSubmitting(isSubmitting) {
        this.isSubmitting = isSubmitting;
        this.submitBtn.disabled = isSubmitting;
        this.submitBtn.textContent = isSubmitting ? 'Processing...' : this.config.submitText;
    }

    getFormData() {
        const data = {};
        this.config.fields.forEach(field => {
            data[field.name || field.id] = this.inputs[field.id].value.trim();
        });
        return data;
    }

    handleError(error) {
        console.error('Form submission error:', error);
        this.showFormError(error.message || 'An error occurred. Please try again.');
    }

    showFormError(message) {
        if (this.errorContainer) {
            this.errorContainer.textContent = message;
            this.errorContainer.classList.add('visible');
        }
    }
}