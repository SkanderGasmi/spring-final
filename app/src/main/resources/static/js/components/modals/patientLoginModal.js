// patientLoginModal.js
import { patientLogin } from '../../services/patientServices.js';

export function renderPatientLogin(container) {
    container.innerHTML = `
    <h2>Patient Login</h2>
    <input type="email" id="email" placeholder="Email" class="input-field">
    <input type="password" id="password" placeholder="Password" class="input-field">
    <button class="dashboard-btn" id="loginBtn">Login</button>
    <div id="loginError" style="color:red; margin-top:10px;"></div>
  `;

    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const errorDiv = document.getElementById('loginError');

    document.getElementById('loginBtn').addEventListener('click', async () => {
        const email = emailInput.value.trim();
        const password = passwordInput.value;

        if (!email || !password) return (errorDiv.textContent = 'All fields are required.');

        try {
            const response = await patientLogin({ email, password });
            if (response.error) {
                errorDiv.textContent = response.error;
            } else {
                localStorage.setItem('token', response.token);
                localStorage.setItem('userRole', 'loggedPatient');
                window.location.href = '/pages/loggedPatientDashboard.html';
            }
        } catch {
            errorDiv.textContent = 'Login failed. Please try again.';
        }
    });
}
