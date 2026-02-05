// doctorLoginModal.js
import { doctorLoginHandler } from '../../services/index.js';

export function renderDoctorLogin(container) {
    container.innerHTML = `
    <h2>Doctor Login</h2>
    <input type="email" id="doctorEmail" placeholder="Email" class="input-field">
    <input type="password" id="doctorPassword" placeholder="Password" class="input-field">
    <button class="dashboard-btn" id="doctorLoginBtn">Login</button>
    <div id="doctorLoginError" style="color:red; margin-top:10px;"></div>
  `;

    document.getElementById('doctorLoginBtn').addEventListener('click', doctorLoginHandler);
}
