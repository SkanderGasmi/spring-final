// header.js - NO INFINITE LOOP VERSION
function renderHeader() {
  const headerDiv = document.getElementById("header");
  if (!headerDiv) return;

  if (window.location.pathname.endsWith("/") || window.location.pathname.includes("index.html")) {
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

  const role = localStorage.getItem("userRole");
  const token = localStorage.getItem("token");

  let headerContent = `<header class="header">
        <div class="logo-section">
            <img src="./assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
            <span class="logo-title">Hospital CMS</span>
        </div>
        <nav>`;

  const isDashboardPage = window.location.pathname.includes("Dashboard");

  if (isDashboardPage && (role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
    localStorage.removeItem("userRole");
    alert("Session expired or invalid login. Please log in again.");
    window.location.href = "/";
    return;
  }

  if (role === "admin") {
    headerContent += `
            <button id="addDocBtn" class="adminBtn" onclick="callModal('addDoctor')">Add Doctor</button>
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

  headerContent += `</nav></header>`;
  headerDiv.innerHTML = headerContent;
  attachHeaderButtonListeners();
}

// NEW: Safe function that won't cause infinite loop
function callModal(modalType) {
  console.log("Calling modal:", modalType);
  if (typeof window.openModal === 'function') {
    window.openModal(modalType);
  } else {
    console.error("Modal system not loaded");
  }
}

function showLoginModal(role) {
  console.log("Opening login modal for:", role);
  let modalType = '';
  if (role === 'admin') modalType = 'adminLogin';
  else if (role === 'doctor') modalType = 'doctorLogin';
  callModal(modalType);
}

function selectRole(role) {
  console.log("Role selected:", role);
  localStorage.removeItem("userRole");
  localStorage.removeItem("token");
  sessionStorage.setItem("tempRole", role);

  if (role === "admin" || role === "doctor") {
    showLoginModal(role);
  } else if (role === "patient") {
    window.location.href = "/patientDashboard";
  }
}

function attachHeaderButtonListeners() {
  const patientLoginBtn = document.getElementById("patientLogin");
  const patientSignupBtn = document.getElementById("patientSignup");

  if (patientLoginBtn) {
    patientLoginBtn.addEventListener("click", function () {
      callModal('patientLogin');
    });
  }

  if (patientSignupBtn) {
    patientSignupBtn.addEventListener("click", function () {
      callModal('patientSignup');
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

// Login success handler
function handleLoginSuccess(token, role) {
  console.log("Login success - storing credentials");
  const selectedRole = sessionStorage.getItem("tempRole") || role;
  localStorage.setItem("userRole", selectedRole);
  localStorage.setItem("token", token);
  sessionStorage.removeItem("tempRole");
  window.location.href = `/${selectedRole.toLowerCase()}Dashboard?token=${token}`;
}

// Remove the problematic openModal function entirely

document.addEventListener("DOMContentLoaded", renderHeader);