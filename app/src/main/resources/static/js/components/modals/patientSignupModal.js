// patientSignupModal.js

import { patientSignup } from '../../services/patientServices.js'; // your API call

// Utility functions for validation
function validateName(name) {
  return name.trim().length > 0;
}

function validateEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function validatePassword(password) {
  return password.length >= 6;
}

function validatePhone(phone) {
  return /^\d{10}$/.test(phone);
}

function validateAddress(address) {
  return address.trim().length > 0;
}

// Apply live validation on input
function addLiveValidation(input, validator) {
  input.addEventListener('input', () => {
    if (validator(input.value)) {
      input.style.borderColor = 'green';
    } else {
      input.style.borderColor = 'red';
    }
  });
}

// Render the signup form
export function renderPatientSignup(container) {
  container.innerHTML = `
    <h2>Patient Signup</h2>
    <input type="text" id="name" placeholder="Name" class="input-field">
    <input type="email" id="email" placeholder="Email" class="input-field">
    <input type="password" id="password" placeholder="Password" class="input-field">
    <input type="text" id="phone" placeholder="Phone" class="input-field">
    <input type="text" id="address" placeholder="Address" class="input-field">
    <button class="dashboard-btn" id="signupBtn">Signup</button>
    <div id="signupError" style="color:red; margin-top:10px;"></div>
  `;

  // Grab inputs
  const nameInput = document.getElementById('name');
  const emailInput = document.getElementById('email');
  const passwordInput = document.getElementById('password');
  const phoneInput = document.getElementById('phone');
  const addressInput = document.getElementById('address');
  const errorDiv = document.getElementById('signupError');

  // Attach live validation
  addLiveValidation(nameInput, validateName);
  addLiveValidation(emailInput, validateEmail);
  addLiveValidation(passwordInput, validatePassword);
  addLiveValidation(phoneInput, validatePhone);
  addLiveValidation(addressInput, validateAddress);

  // Handle form submission
  document.getElementById('signupBtn').addEventListener('click', async () => {
    const name = nameInput.value.trim();
    const email = emailInput.value.trim();
    const password = passwordInput.value;
    const phone = phoneInput.value.trim();
    const address = addressInput.value.trim();

    // Frontend validation before sending
    if (!validateName(name)) return (errorDiv.textContent = 'Name is required.');
    if (!validateEmail(email)) return (errorDiv.textContent = 'Invalid email.');
    if (!validatePassword(password)) return (errorDiv.textContent = 'Password must be at least 6 characters.');
    if (!validatePhone(phone)) return (errorDiv.textContent = 'Phone number must be 10 digits.');
    if (!validateAddress(address)) return (errorDiv.textContent = 'Address is required.');

    errorDiv.textContent = '';

    try {
      const response = await patientSignup({ name, email, password, phone, address });
      if (response.error) {
        errorDiv.textContent = response.error; // server-side error
      } else {
        errorDiv.style.color = 'green';
        errorDiv.textContent = 'Signup successful!';
        // Optionally close modal or redirect
      }
    } catch (err) {
      errorDiv.textContent = 'Signup failed. Please try again later.';
    }
  });
}
