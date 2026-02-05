// render.js

// Called when user clicks a role button on index.html
function selectRole(role) {
  // Use the setRole from util.js (available globally)
  if (typeof setRole === 'function') {
    setRole(role);
  } else {
    localStorage.setItem('userRole', role);
  }

  // Patients go directly to dashboard
  if (role === 'patient') {
    window.location.href = '/pages/patientDashboard.html';
    return;
  }

  // Admin & Doctor need login → open modal
  openModal(role);
}

// Called on admin/doctor dashboard pages to check login
function renderContent() {
  const role = getRole ? getRole() : localStorage.getItem('userRole');
  if (!role) {
    window.location.href = '/index.html';
    return;
  }

  const token = localStorage.getItem('token');

  // All roles need token for dashboard access
  if (!token) {
    alert('Please login first!');
    window.location.href = '/index.html';
    return;
  }

  // Additional role-specific checks if needed
  if ((role === 'admin' || role === 'doctor') && !token) {
    window.location.href = '/index.html';
  }
}

// Modal functions
function openModal(role) {
  const modal = document.getElementById('modal');
  const modalBody = document.getElementById('modal-body');

  if (!modal || !modalBody) return;

  modalBody.innerHTML = `
    <h3>${role.charAt(0).toUpperCase() + role.slice(1)} Login</h3>
    <form id="loginForm">
      <input type="email" placeholder="Email" required>
      <input type="password" placeholder="Password" required>
      <button type="submit">Login</button>
    </form>
  `;

  modal.style.display = 'block';

  // Handle form submission
  const form = document.getElementById('loginForm');
  if (form) {
    form.addEventListener('submit', function (e) {
      e.preventDefault();
      const email = this.querySelector('input[type="email"]').value;
      const password = this.querySelector('input[type="password"]').value;

      // Call login service
      if (typeof login === 'function') {
        login(email, password, role);
      }
    });
  }
}

function closeModal() {
  const modal = document.getElementById('modal');
  if (modal) {
    modal.style.display = 'none';
  }
}

// Add event listener for modal close button
document.addEventListener('DOMContentLoaded', function () {
  const closeBtn = document.getElementById('closeModal');
  if (closeBtn) {
    closeBtn.addEventListener('click', closeModal);
  }

  // Close modal when clicking outside
  const modal = document.getElementById('modal');
  if (modal) {
    window.addEventListener('click', function (event) {
      if (event.target === modal) {
        closeModal();
      }
    });
  }
});

