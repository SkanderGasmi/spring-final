// index.js - Role-Based Login Handling

// Import the openModal function to handle showing login popups/modals
import { openModal } from '../components/modals.js';

// Import the base API URL from the config file
import { API_BASE_URL } from '../config/config.js';

// Define constants for the admin and doctor login API endpoints using the base URL
const ADMIN_API = API_BASE_URL + '/admin';
const DOCTOR_API = API_BASE_URL + '/doctor/login';

// Use the window.onload event to ensure DOM elements are available after page load
window.onload = function () {
  // Select the "adminLogin" and "doctorLogin" buttons using getElementById
  const adminBtn = document.getElementById('adminLogin');
  const doctorBtn = document.getElementById('doctorLogin');

  // If the admin login button exists:
  if (adminBtn) {
    // Add a click event listener that calls openModal('adminLogin') to show the admin login modal
    adminBtn.addEventListener('click', () => {
      openModal('adminLogin');
    });
  }

  // If the doctor login button exists:
  if (doctorBtn) {
    // Add a click event listener that calls openModal('doctorLogin') to show the doctor login modal
    doctorBtn.addEventListener('click', () => {
      openModal('doctorLogin');
    });
  }
};

// Define a function named adminLoginHandler on the global window object
// This function will be triggered when the admin submits their login credentials
window.adminLoginHandler = async function () {
  // Step 1: Get the entered username and password from the input fields
  const username = document.getElementById('adminUsername').value;
  const password = document.getElementById('adminPassword').value;

  // Step 2: Create an admin object with these credentials
  const admin = { username, password };

  // Step 3: Use fetch() to send a POST request to the ADMIN_API endpoint
  try {
    const response = await fetch(ADMIN_API, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(admin)
    });

    // Step 4: If the response is successful:
    if (response.ok) {
      // Parse the JSON response to get the token
      const data = await response.json();
      const token = data.token;

      // Store the token in localStorage
      localStorage.setItem('token', token);

      // Call selectRole('admin') to proceed with admin-specific behavior
      selectRole('admin');
    } else {
      // Step 5: If login fails or credentials are invalid:
      // Show an alert with an error message
      alert("Invalid credentials!");
    }
  } catch (error) {
    // Step 6: Wrap everything in a try-catch to handle network or server errors
    // Show a generic error message if something goes wrong
    console.error('Login error:', error);
    alert("Something went wrong. Please try again.");
  }
};

// Define a function named doctorLoginHandler on the global window object
// This function will be triggered when a doctor submits their login credentials
window.doctorLoginHandler = async function () {
  // Step 1: Get the entered email and password from the input fields
  const email = document.getElementById('doctorEmail').value;
  const password = document.getElementById('doctorPassword').value;

  // Step 2: Create a doctor object with these credentials
  const doctor = { email, password };

  // Step 3: Use fetch() to send a POST request to the DOCTOR_API endpoint
  try {
    const response = await fetch(DOCTOR_API, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(doctor)
    });

    // Step 4: If login is successful:
    if (response.ok) {
      // Parse the JSON response to get the token
      const data = await response.json();
      const token = data.token;

      // Store the token in localStorage
      localStorage.setItem('token', token);

      // Call selectRole('doctor') to proceed with doctor-specific behavior
      selectRole('doctor');
    } else {
      // Step 5: If login fails:
      // Show an alert for invalid credentials
      alert("Invalid credentials!");
    }
  } catch (error) {
    // Step 6: Wrap in a try-catch block to handle errors gracefully
    // Log the error to the console
    console.error('Login error:', error);
    // Show a generic error message
    alert("Something went wrong. Please try again.");
  }
};

// Helper function to select role (would be in render.js)
function selectRole(role) {
  localStorage.setItem('userRole', role);
  // Redirect based on role
  if (role === 'admin') {
    window.location.href = '/admin/adminDashboard.html';
  } else if (role === 'doctor') {
    window.location.href = '/doctor/doctorDashboard.html';
  }
}