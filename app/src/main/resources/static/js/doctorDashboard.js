// doctorDashboard.js – Managing Appointments

// Import Required Modules
import { getAllAppointments } from './services/appointmentRecordService.js';
import { createPatientRow } from './components/patientRows.js';

// Initialize Global Variables
const tableBody = document.getElementById('patientTableBody');
let selectedDate = new Date().toISOString().split('T')[0]; // Today's date in YYYY-MM-DD
const token = localStorage.getItem('token');
let patientName = null;

// Setup Search Bar Functionality
const searchBar = document.getElementById('searchBar');
if (searchBar) {
  searchBar.addEventListener('input', function () {
    // On each keystroke:
    // - Trim and check the input value
    const inputValue = this.value.trim();

    // - If not empty, use it as the patientName for filtering
    // - Else, reset patientName to "null" (as expected by backend)
    patientName = inputValue ? inputValue : "null";

    // - Reload the appointments list with the updated filter
    loadAppointments();
  });
}

// Bind Event Listeners to Filter Controls
const todayButton = document.getElementById('todayButton');
if (todayButton) {
  // Add a click listener to the "Today" button
  todayButton.addEventListener('click', function () {
    // When clicked:
    // - Set selectedDate to today's date
    selectedDate = new Date().toISOString().split('T')[0];

    // - Update the date picker UI to match
    const datePicker = document.getElementById('datePicker');
    if (datePicker) {
      datePicker.value = selectedDate;
    }

    // - Reload the appointments for today
    loadAppointments();
  });
}

const datePicker = document.getElementById('datePicker');
if (datePicker) {
  // Add a change event listener to the date picker
  datePicker.addEventListener('change', function () {
    // When the date changes:
    // - Update selectedDate with the new value
    selectedDate = this.value;

    // - Reload the appointments for that specific date
    loadAppointments();
  });
}

// Function: loadAppointments
// Purpose: Fetch and display appointments based on selected date and optional patient name
async function loadAppointments() {
  try {
    // Step 1: Call getAllAppointments with selectedDate, patientName, and token
    const appointments = await getAllAppointments(selectedDate, patientName, token);

    // Step 2: Clear the table body content before rendering new rows
    tableBody.innerHTML = '';

    // Step 3: If no appointments are returned:
    if (!appointments || appointments.length === 0) {
      // Display a message row: "No Appointments found for today."
      const noAppointmentsRow = document.createElement('tr');
      noAppointmentsRow.innerHTML = `
                <td colspan="5" class="no-appointments">
                    No appointments found for ${selectedDate}.
                </td>
            `;
      tableBody.appendChild(noAppointmentsRow);
      return;
    }

    // Step 4: If appointments exist:
    appointments.forEach(appointment => {
      // - Loop through each appointment and construct a 'patient' object with id, name, phone, and email
      const patient = {
        id: appointment.patientId || appointment.id,
        name: appointment.patientName || 'Unknown Patient',
        phone: appointment.patientPhone || 'N/A',
        email: appointment.patientEmail || 'N/A',
        appointmentId: appointment.id
      };

      // - Call createPatientRow to generate a table row for the appointment
      const row = createPatientRow(patient);

      // - Append each row to the table body
      tableBody.appendChild(row);
    });

  } catch (error) {
    // Step 5: Catch and handle any errors during fetch:
    console.error('Error loading appointments:', error);

    // Show a message row: "Error loading appointments. Try again later."
    tableBody.innerHTML = '';
    const errorRow = document.createElement('tr');
    errorRow.innerHTML = `
            <td colspan="5" class="error-message">
                Error loading appointments. Try again later.
            </td>
        `;
    tableBody.appendChild(errorRow);
  }
}

// Initial Render on Page Load
document.addEventListener('DOMContentLoaded', function () {
  // Call renderContent() (assumes it sets up the UI layout)
  if (typeof renderContent === 'function') {
    renderContent();
  }

  // Set initial date picker value
  if (datePicker) {
    datePicker.value = selectedDate;
  }

  // Call loadAppointments() to display today's appointments by default
  loadAppointments();
});

// Export for testing if needed
if (typeof window !== 'undefined') {
  window.loadAppointments = loadAppointments;
}