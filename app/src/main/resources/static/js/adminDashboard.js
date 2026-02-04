// adminDashboard.js - Managing Doctors

// Import Required Modules
import { openModal } from './components/modals.js';
import { getDoctors, filterDoctors, saveDoctor } from './services/doctorServices.js';
import { createDoctorCard } from './components/doctorCard.js';

// Event Binding
document.addEventListener('DOMContentLoaded', function () {
  const addDocBtn = document.getElementById('addDocBtn');
  if (addDocBtn) {
    addDocBtn.addEventListener('click', () => {
      openModal('addDoctor');
    });
  }

  // Load doctor cards on page load
  loadDoctorCards();

  // Set up search and filter event listeners
  document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange);
  document.getElementById("filterTime").addEventListener("change", filterDoctorsOnChange);
  document.getElementById("filterSpecialty").addEventListener("change", filterDoctorsOnChange);
});

// Function: loadDoctorCards
// Purpose: Fetch all doctors and display them as cards
async function loadDoctorCards() {
  try {
    // Call getDoctors() from the service layer
    const doctors = await getDoctors();

    // Clear the current content area
    const contentDiv = document.getElementById("content");
    contentDiv.innerHTML = "";

    // For each doctor returned:
    // - Create a doctor card using createDoctorCard()
    // - Append it to the content div
    doctors.forEach(doctor => {
      const card = createDoctorCard(doctor);
      contentDiv.appendChild(card);
    });

    // If no doctors found
    if (doctors.length === 0) {
      contentDiv.innerHTML = '<p class="no-doctors">No doctors found</p>';
    }
  } catch (error) {
    // Handle any fetch errors by logging them
    console.error('Error loading doctor cards:', error);
    const contentDiv = document.getElementById("content");
    contentDiv.innerHTML = '<p class="error">Error loading doctors. Please try again.</p>';
  }
}

// Attach 'input' and 'change' event listeners to the search bar and filter dropdowns
// On any input change, call filterDoctorsOnChange()

// Function: filterDoctorsOnChange
// Purpose: Filter doctors based on name, available time, and specialty
async function filterDoctorsOnChange() {
  try {
    // Read values from the search bar and filters
    const name = document.getElementById("searchBar").value.trim();
    const time = document.getElementById("filterTime").value;
    const specialty = document.getElementById("filterSpecialty").value;

    // Normalize empty values to null
    const searchName = name === "" ? null : name;
    const searchTime = time === "" ? null : time;
    const searchSpecialty = specialty === "" ? null : specialty;

    // Call filterDoctors(name, time, specialty) from the service
    const doctors = await filterDoctors(searchName, searchTime, searchSpecialty);

    // If doctors are found:
    if (doctors && doctors.length > 0) {
      // - Render them using createDoctorCard()
      renderDoctorCards(doctors);
    } else {
      // If no doctors match the filter:
      // - Show a message: "No doctors found with the given filters."
      const contentDiv = document.getElementById("content");
      contentDiv.innerHTML = '<p class="no-doctors">No doctors found with the given filters.</p>';
    }
  } catch (error) {
    // Catch and display any errors with an alert
    console.error('Error filtering doctors:', error);
    alert('Error filtering doctors. Please try again.');
  }
}

// Function: renderDoctorCards
// Purpose: A helper function to render a list of doctors passed to it
function renderDoctorCards(doctors) {
  // Clear the content area
  const contentDiv = document.getElementById("content");
  contentDiv.innerHTML = "";

  // Loop through the doctors and append each card to the content area
  doctors.forEach(doctor => {
    const card = createDoctorCard(doctor);
    contentDiv.appendChild(card);
  });
}

// Function: adminAddDoctor
// Purpose: Collect form data and add a new doctor to the system
window.adminAddDoctor = async function () {
  try {
    // Collect input values from the modal form
    // - Includes name, email, phone, password, specialty, and available times
    const name = document.getElementById('doctorName').value.trim();
    const email = document.getElementById('doctorEmail').value.trim();
    const phone = document.getElementById('doctorPhone').value.trim();
    const password = document.getElementById('doctorPassword').value;
    const specialty = document.getElementById('doctorSpecialty').value.trim();
    const availability = document.getElementById('doctorAvailability').value.trim();

    // Validate required fields
    if (!name || !email || !phone || !password || !specialty) {
      alert('Please fill in all required fields.');
      return;
    }

    // Retrieve the authentication token from localStorage
    const token = localStorage.getItem('token');

    // - If no token is found, show an alert and stop execution
    if (!token) {
      alert('Please log in as admin to add a doctor.');
      return;
    }

    // Build a doctor object with the form values
    const doctor = {
      name: name,
      email: email,
      phone: phone,
      password: password,
      specialty: specialty,
      availableTimes: availability.split(',').map(time => time.trim())
    };

    // Call saveDoctor(doctor, token) from the service
    const result = await saveDoctor(doctor, token);

    // If save is successful:
    if (result.success) {
      // - Show a success message
      alert(result.message || 'Doctor added successfully!');

      // - Close the modal and reload the page
      const modal = document.getElementById('modal');
      if (modal) modal.style.display = 'none';

      // Reload the doctor list
      loadDoctorCards();

      // Reset the form
      const form = document.querySelector('#modal-body form');
      if (form) form.reset();
    } else {
      // If saving fails, show an error message
      alert(result.message || 'Failed to add doctor. Please try again.');
    }
  } catch (error) {
    console.error('Error adding doctor:', error);
    alert('An error occurred while adding the doctor. Please try again.');
  }
};

// Helper function to reset modal form
function resetAddDoctorForm() {
  const form = document.querySelector('#modal-body form');
  if (form) {
    form.reset();
  }
}

// Make functions available globally
window.loadDoctorCards = loadDoctorCards;
window.filterDoctorsOnChange = filterDoctorsOnChange;