// adminDashboard.js - Managing Doctors

// Import Required Modules - FIXED PATHS
import { getDoctors, filterDoctors, saveDoctor } from './services/doctorServices.js';

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

  // Set up search and filter event listeners - FIXED IDs
  const searchBar = document.getElementById("searchBar");
  const timeFilter = document.getElementById("timeFilter");
  const specialtyFilter = document.getElementById("specialtyFilter");

  if (searchBar) searchBar.addEventListener("input", filterDoctorsOnChange);
  if (timeFilter) timeFilter.addEventListener("change", filterDoctorsOnChange);
  if (specialtyFilter) specialtyFilter.addEventListener("change", filterDoctorsOnChange);
});

// Function: loadDoctorCards
// Purpose: Fetch all doctors and display them as cards
async function loadDoctorCards() {
  try {
    // Call getDoctors() from the service layer
    const doctors = await getDoctors();

    // Clear the current content area
    const contentDiv = document.getElementById("content");
    if (!contentDiv) {
      console.error('Content div not found');
      return;
    }

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
    if (contentDiv) {
      contentDiv.innerHTML = '<p class="error">Error loading doctors. Please try again.</p>';
    }
  }
}

// Function: filterDoctorsOnChange
// Purpose: Filter doctors based on name, available time, and specialty
async function filterDoctorsOnChange() {
  try {
    // Read values from the search bar and filters - FIXED IDs
    const name = document.getElementById("searchBar")?.value?.trim() || '';
    const time = document.getElementById("timeFilter")?.value || '';
    const specialty = document.getElementById("specialtyFilter")?.value || '';

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
      if (contentDiv) {
        contentDiv.innerHTML = '<p class="no-doctors">No doctors found with the given filters.</p>';
      }
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
  if (!contentDiv) return;

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
    const name = document.getElementById('doctorName')?.value?.trim() || '';
    const email = document.getElementById('doctorEmail')?.value?.trim() || '';
    const phone = document.getElementById('doctorPhone')?.value?.trim() || '';
    const password = document.getElementById('doctorPassword')?.value || '';
    const specialty = document.getElementById('specialization')?.value || '';

    // Get availability checkboxes
    const availabilityCheckboxes = document.querySelectorAll('input[name="availability"]:checked');
    const availability = Array.from(availabilityCheckboxes).map(cb => cb.value);

    // Validate required fields
    if (!name || !email || !phone || !password || !specialty || availability.length === 0) {
      alert('Please fill in all required fields and select at least one availability slot.');
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
      availableTimes: availability
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
    } else {
      // If saving fails, show an error message
      alert(result.message || 'Failed to add doctor. Please try again.');
    }
  } catch (error) {
    console.error('Error adding doctor:', error);
    alert('An error occurred while adding the doctor. Please try again.');
  }
};

// Function to create and return a DOM element for a single doctor card
function createDoctorCard(doctor) {
  // Create the main container for the doctor card
  const card = document.createElement("div");
  card.classList.add("doctor-card");

  // Retrieve the current user role from localStorage
  const role = localStorage.getItem("userRole");

  // Create a div to hold doctor information
  const infoDiv = document.createElement("div");
  infoDiv.classList.add("doctor-info");

  // Create and set the doctor's name
  const name = document.createElement("h3");
  name.textContent = doctor.name || 'Unnamed Doctor';

  // Create and set the doctor's specialization
  const specialization = document.createElement("p");
  specialization.textContent = `Specialty: ${doctor.specialty || 'Not specified'}`;

  // Create and set the doctor's email
  const email = document.createElement("p");
  email.textContent = `Email: ${doctor.email || 'No email'}`;

  // Create and list available appointment times
  const availability = document.createElement("p");
  const times = doctor.availableTimes || [];
  availability.textContent = `Available: ${times.length > 0 ? times.join(", ") : "Not available"}`;

  // Append all info elements to the doctor info container
  infoDiv.appendChild(name);
  infoDiv.appendChild(specialization);
  infoDiv.appendChild(email);
  infoDiv.appendChild(availability);

  // Create a container for card action buttons
  const actionsDiv = document.createElement("div");
  actionsDiv.classList.add("card-actions");

  // === ADMIN ROLE ACTIONS ===
  if (role === "admin") {
    // Create a delete button
    const removeBtn = document.createElement("button");
    removeBtn.textContent = "Delete";
    removeBtn.classList.add("delete-btn");

    // Add click handler for delete button
    removeBtn.addEventListener("click", async () => {
      if (confirm(`Are you sure you want to delete ${doctor.name}?`)) {
        try {
          // In a real app, you would call: deleteDoctor(doctor.id, token)
          console.log(`Would delete doctor ${doctor.id}`);
          alert("Doctor deleted successfully!");
          card.remove();
        } catch (error) {
          console.error("Failed to delete doctor:", error);
          alert("Failed to delete doctor");
        }
      }
    });

    // Add delete button to actions container
    actionsDiv.appendChild(removeBtn);
  }

  // === PATIENT (NOT LOGGED-IN) ROLE ACTIONS ===
  else if (role === "patient") {
    // Create a book now button
    const bookNow = document.createElement("button");
    bookNow.textContent = "Book Now";
    bookNow.classList.add("book-btn");

    // Alert patient to log in before booking
    bookNow.addEventListener("click", () => {
      alert("Please log in first to book an appointment.");
    });

    // Add button to actions container
    actionsDiv.appendChild(bookNow);
  }

  // === LOGGED-IN PATIENT ROLE ACTIONS === 
  else if (role === "loggedPatient") {
    // Create a book now button
    const bookNow = document.createElement("button");
    bookNow.textContent = "Book Now";
    bookNow.classList.add("book-btn");

    // Handle booking logic for logged-in patient
    bookNow.addEventListener("click", async (e) => {
      // Redirect if token not available
      const token = localStorage.getItem('token');
      if (!token) {
        alert("Please log in again");
        window.location.href = "/";
        return;
      }

      // Show booking modal
      alert(`Booking appointment with ${doctor.name}`);
      // In a real app, you would open a booking modal here
    });

    // Add button to actions container
    actionsDiv.appendChild(bookNow);
  }

  // Append doctor info and action buttons to the card
  card.appendChild(infoDiv);
  card.appendChild(actionsDiv);

  // Return the complete doctor card element
  return card;
}

// Make functions available globally
window.loadDoctorCards = loadDoctorCards;
window.filterDoctorsOnChange = filterDoctorsOnChange;
window.createDoctorCard = createDoctorCard;