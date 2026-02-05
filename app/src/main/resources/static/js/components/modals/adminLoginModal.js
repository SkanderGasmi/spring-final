// adminLoginModal.js
import { adminLoginHandler } from '../../services/index.js';

export function renderAdminLogin(container) {
    container.innerHTML = `
    <h2>Admin Login</h2>
    <input type="text" id="adminUsername" placeholder="Username" class="input-field">
    <input type="password" id="adminPassword" placeholder="Password" class="input-field">
    <button class="dashboard-btn" id="adminLoginBtn">Login</button>
    <div id="adminLoginError" style="color:red; margin-top:10px;"></div>
  `;

    document.getElementById('adminLoginBtn').addEventListener('click', adminLoginHandler);
}
