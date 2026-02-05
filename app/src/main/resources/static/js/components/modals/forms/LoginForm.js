// forms/LoginForm.js
import { BaseForm } from './BaseForm.js';
import { Validator } from '../validators/Validator.js';
import { AuthService } from '../services/AuthService.js';

export class LoginForm extends BaseForm {
    constructor(userType) {
        const configs = {
            patient: {
                title: 'Patient Login',
                fields: [
                    {
                        id: 'email',
                        type: 'email',
                        label: 'Email',
                        placeholder: 'Enter your email',
                        required: true,
                        validation: 'email'
                    },
                    {
                        id: 'password',
                        type: 'password',
                        label: 'Password',
                        placeholder: 'Enter your password',
                        required: true
                    }
                ],
                submitId: 'patientLoginBtn',
                errorId: 'patientLoginError',
                submitText: 'Login',
                onSubmit: AuthService.patientLogin,
                redirectUrl: '/pages/loggedPatientDashboard.html',
                userRole: 'patient'
            },
            admin: {
                title: 'Admin Login',
                fields: [
                    {
                        id: 'username',
                        type: 'text',
                        label: 'Username',
                        placeholder: 'Enter admin username',
                        required: true
                    },
                    {
                        id: 'password',
                        type: 'password',
                        label: 'Password',
                        placeholder: 'Enter admin password',
                        required: true
                    }
                ],
                submitId: 'adminLoginBtn',
                errorId: 'adminLoginError',
                submitText: 'Login',
                onSubmit: AuthService.adminLogin,
                redirectUrl: '/adminDashboard',
                userRole: 'admin'
            },
            doctor: {
                title: 'Doctor Login',
                fields: [
                    {
                        id: 'email',
                        type: 'email',
                        label: 'Email',
                        placeholder: 'Enter doctor email',
                        required: true,
                        validation: 'email'
                    },
                    {
                        id: 'password',
                        type: 'password',
                        label: 'Password',
                        placeholder: 'Enter doctor password',
                        required: true
                    }
                ],
                submitId: 'doctorLoginBtn',
                errorId: 'doctorLoginError',
                submitText: 'Login',
                onSubmit: AuthService.doctorLogin,
                redirectUrl: '/doctorDashboard',
                userRole: 'doctor'
            }
        };

        super(configs[userType]);
        this.userType = userType;
        this.validator = new Validator();
    }

    setupValidation() {
        super.setupValidation();

        // Add real-time validation
        this.config.fields.forEach(field => {
            if (field.validation) {
                this.inputs[field.id].addEventListener('blur', () => {
                    this.validateField(field);
                });
            }
        });
    }

    validateField(field) {
        const value = this.inputs[field.id].value.trim();
        const validationRule = field.validation;

        if (validationRule && value) {
            const isValid = this.validator.validate(value, validationRule);
            if (!isValid) {
                const message = this.validator.getErrorMessage(validationRule);
                this.showFieldError(field.id, message);
                return false;
            }
        }

        this.clearFieldError(field.id);
        return true;
    }

    async handleSubmit() {
        if (!await this.validateAllAsync()) return;

        await super.handleSubmit();
    }

    async validateAllAsync() {
        let isValid = true;

        for (const field of this.config.fields) {
            if (!this.validateField(field)) {
                isValid = false;
            }
        }

        return isValid;
    }
}

// Factory function for backward compatibility
export function createLoginForm(userType) {
    return (container) => {
        const form = new LoginForm(userType);
        form.render(container);
    };
}