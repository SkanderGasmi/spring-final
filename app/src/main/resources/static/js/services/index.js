import { API_BASE_URL } from '../config/config.js';

// CORRECT ENDPOINTS - MUST INCLUDE /api/ prefix
const ADMIN_API = API_BASE_URL + '/api/admin/login';
const DOCTOR_API = API_BASE_URL + '/api/doctor/login';
const PATIENT_API = API_BASE_URL + '/api/patient/login';

export async function login(email, password, role) {
  let endpoint;
  let requestBody;

  switch (role) {
    case 'admin':
      endpoint = ADMIN_API;
      requestBody = { username: email, password };
      break;
    case 'doctor':
      endpoint = DOCTOR_API;
      requestBody = { email, password };
      break;
    case 'patient':
      endpoint = PATIENT_API;
      requestBody = { email, password };
      break;
    default:
      throw new Error('Invalid role');
  }

  console.log('Calling endpoint:', endpoint); // Debug logging

  try {
    const res = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody)
    });

    console.log('Response status:', res.status); // Debug logging

    if (!res.ok) {
      const error = await res.text();
      throw new Error(error || 'Login failed');
    }

    const data = await res.json();
    console.log('Login response:', data); // Debug logging

    if (!data.token) {
      throw new Error('No token received');
    }

    // Store token
    localStorage.setItem('token', data.token);

    // Redirect with token as query parameter
    switch (role) {
      case 'admin':
        window.location.href = `/adminDashboard?token=${data.token}`;
        break;
      case 'doctor':
        window.location.href = `/doctorDashboard?token=${data.token}`;
        break;
      case 'patient':
        window.location.href = `/patientDashboard?token=${data.token}`;
        break;
    }

  } catch (err) {
    console.error('Login error:', err);
    alert('Login failed: ' + err.message);
    throw err;
  }
}

// Specific handler functions for modals.js
export async function adminLoginHandler() {
  const username = document.getElementById('username')?.value;
  const password = document.getElementById('password')?.value;

  if (username && password) {
    try {
      await login(username, password, 'admin');
    } catch (err) {
      console.error('Admin login failed:', err);
      alert('Admin login failed: ' + err.message);
    }
  } else {
    alert('Please enter username and password');
  }
}

export async function doctorLoginHandler() {
  const email = document.getElementById('email')?.value;
  const password = document.getElementById('password')?.value;

  if (email && password) {
    try {
      await login(email, password, 'doctor');
    } catch (err) {
      console.error('Doctor login failed:', err);
      alert('Doctor login failed: ' + err.message);
    }
  } else {
    alert('Please enter email and password');
  }
}

export async function patientLoginHandler() {
  const email = document.getElementById('email')?.value;
  const password = document.getElementById('password')?.value;

  if (email && password) {
    try {
      await login(email, password, 'patient');
    } catch (err) {
      console.error('Patient login failed:', err);
      alert('Patient login failed: ' + err.message);
    }
  } else {
    alert('Please enter email and password');
  }
}

// CRITICAL: Export to global scope immediately
window.login = login;
window.adminLoginHandler = adminLoginHandler;
window.doctorLoginHandler = doctorLoginHandler;
window.patientLoginHandler = patientLoginHandler;

// Placeholder functions to prevent errors
window.adminAddDoctor = function () {
  console.log('Add doctor - implement this function');
  alert('Add doctor functionality not implemented yet');
};

window.signupPatient = function () {
  console.log('Patient signup - implement this function');
  alert('Patient signup functionality not implemented yet');
};

window.loginPatient = patientLoginHandler; // Alias for patient login

console.log("Services loaded and global functions exported");