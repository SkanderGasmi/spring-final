// utils/sanitizer.js
export class Sanitizer {
    static sanitizeInput(input) {
        if (typeof input !== 'string') return input;

        return input
            .replace(/[<>]/g, '') // Remove HTML tags
            .trim()
            .substring(0, 255); // Limit length
    }

    static sanitizeObject(obj) {
        const sanitized = {};

        for (const [key, value] of Object.entries(obj)) {
            if (typeof value === 'string') {
                sanitized[key] = this.sanitizeInput(value);
            } else {
                sanitized[key] = value;
            }
        }

        return sanitized;
    }
}