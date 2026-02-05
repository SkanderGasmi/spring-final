// services/AuthService.js
import { ApiService } from './ApiService.js';

export class AuthService {
    static api = new ApiService();

    static async patientLogin(credentials) {
        try {
            const response = await this.api.post('/patient/login', credentials);

            if (response.token) {
                localStorage.setItem('token', response.token);
                localStorage.setItem('userRole', 'patient');
                localStorage.setItem('userData', JSON.stringify(response.user));

                // Dispatch custom event for other components
                window.dispatchEvent(new CustomEvent('auth:login', {
                    detail: { role: 'patient', user: response.user }
                }));

                return { success: true, redirect: '/pages/loggedPatientDashboard.html' };
            }

            throw new Error(response.error || 'Login failed');
        } catch (error) {
            return { success: false, error: error.message };
        }
    }

    static async adminLogin(credentials) {
        // Similar implementation
    }

    static async doctorLogin(credentials) {
        // Similar implementation
    }

    static async patientSignup(data) {
        try {
            const response = await this.api.post('/patient/signup', data);

            if (response.success) {
                return { success: true, message: 'Signup successful!' };
            }

            throw new Error(response.error || 'Signup failed');
        } catch (error) {
            return { success: false, error: error.message };
        }
    }

    static logout() {
        localStorage.clear();
        window.dispatchEvent(new CustomEvent('auth:logout'));
    }
}