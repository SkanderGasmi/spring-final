// validators/Validator.js
export class Validator {
    constructor() {
        this.rules = {
            email: {
                test: (value) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value),
                message: 'Please enter a valid email address'
            },
            password: {
                test: (value) => value.length >= 6,
                message: 'Password must be at least 6 characters'
            },
            phone: {
                test: (value) => /^\d{10}$/.test(value),
                message: 'Phone number must be 10 digits'
            },
            required: {
                test: (value) => value.trim().length > 0,
                message: 'This field is required'
            },
            name: {
                test: (value) => value.trim().length >= 2,
                message: 'Name must be at least 2 characters'
            }
        };
    }

    validate(value, ruleName) {
        const rule = this.rules[ruleName];
        if (!rule) {
            console.warn(`Validation rule "${ruleName}" not found`);
            return true;
        }
        return rule.test(value);
    }

    validateAll(data, schema) {
        const errors = {};

        for (const [field, rules] of Object.entries(schema)) {
            for (const rule of rules) {
                if (!this.validate(data[field], rule)) {
                    errors[field] = this.getErrorMessage(rule);
                    break;
                }
            }
        }

        return {
            isValid: Object.keys(errors).length === 0,
            errors
        };
    }

    getErrorMessage(ruleName) {
        return this.rules[ruleName]?.message || 'Invalid value';
    }

    addRule(name, test, message) {
        this.rules[name] = { test, message };
    }
}

// Usage schemas
export const VALIDATION_SCHEMAS = {
    patientSignup: {
        name: ['required', 'name'],
        email: ['required', 'email'],
        password: ['required', 'password'],
        phone: ['required', 'phone'],
        address: ['required']
    },
    login: {
        email: ['required', 'email'],
        password: ['required']
    }
};