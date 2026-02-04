// header.js
function renderHeader() {
  const headerDiv = document.getElementById("header");
  if (!headerDiv) return;

  // Check if the current page is the root page
  if (window.location.pathname.endsWith("/")) {
    localStorage.removeItem("userRole");
    localStorage.removeItem("token");
    headerDiv.innerHTML = `
            <header class="header">
                <div class="logo-section">
                    <img src="./assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
                    <span class="logo-title">Hospital CMS</span>
                </div>
            </header>`;
    return;
  }

  // Get user role and token from localStorage
  const role = localStorage.getItem("userRole");
  const token = localStorage.getItem("token");

  // Initialize header content with logo section
  let headerContent = `<header class="header">
        <div class="logo-section">
            <img src="./assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
            <span class="logo-title">Hospital CMS</span>
        </div>
        <nav>`;

  // Handle session expiry or invalid login
  if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
    localStorage.removeItem("userRole");
    alert("Session expired or invalid login. Please log in again.");
    window.location.href = "/";
    return;
  }

  // Add role-specific header content
  if (role === "admin") {
    headerContent += `
            <button id="addDocBtn" class="adminBtn" onclick="openModal('addDoctor')">Add Doctor</button>
            <a href="#" onclick="logout()">Logout</a>`;
  } else if (role === "doctor") {
    headerContent += `
            <button class="adminBtn" onclick="selectRole('doctor')">Home</button>
            <a href="#" onclick="logout()">Logout</a>`;
  } else if (role === "patient") {
    headerContent += `
            <button id="patientLogin" class="adminBtn">Login</button>
            <button id="patientSignup" class="adminBtn">Sign Up</button>`;
  } else if (role === "loggedPatient") {
    headerContent += `
            <button id="home" class="adminBtn" onclick="window.location.href='/pages/loggedPatientDashboard.html'">Home</button>
            <button id="patientAppointments" class="adminBtn" onclick="window.location.href='/pages/patientAppointments.html'">Appointments</button>
            <a href="#" onclick="logoutPatient()">Logout</a>`;
  }

  // Close the header section
  headerContent += `</nav></header>`;

  // Render the header content
  headerDiv.innerHTML = headerContent;

  // Attach event listeners to header buttons
  attachHeaderButtonListeners();
}

function attachHeaderButtonListeners() {
  const patientLoginBtn = document.getElementById("patientLogin");
  const patientSignupBtn = document.getElementById("patientSignup");

  if (patientLoginBtn) {
    patientLoginBtn.addEventListener("click", function () {
      openModal('patientLogin');
    });
  }

  if (patientSignupBtn) {
    patientSignupBtn.addEventListener("click", function () {
      openModal('patientSignup');
    });
  }
}

function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("userRole");
  window.location.href = "/";
}

function logoutPatient() {
  localStorage.removeItem("token");
  localStorage.setItem("userRole", "patient");
  window.location.href = "/pages/patientDashboard.html";
}

function openModal(modalType) {
  console.log("Open modal for:", modalType);
  // This function would be implemented in modals.js
}

function selectRole(role) {
  localStorage.setItem("selectedRole", role);
  window.location.href = "/";
}

// Call renderHeader when the page loads
document.addEventListener("DOMContentLoaded", renderHeader);