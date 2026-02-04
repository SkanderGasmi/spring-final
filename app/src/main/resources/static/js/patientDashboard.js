3. patientDashboard.js – Viewing & Filtering Doctors
Open the JS File:
Open patientDashboard.js located at: app / src / main / resources / static / js / patientDashboard.js
 Open patientDashboard.js in IDE

Import Required Modules
At the top of the file, import:

createDoctorCard from./ components / doctorCard.js – for rendering doctor information cards.
openModal from./ components / modals.js – for opening login / signup modals.
  getDoctors, filterDoctors from./ services / doctorServices.js – for retrieving doctor data.
    patientLogin, patientSignup from./ services / patientServices.js – for handling patient authentication.
Load Doctor Cards on Page Load
When the page loads:

Call loadDoctorCards() inside a DOMContentLoaded event listener.

This function:

  Calls getDoctors() to fetch the list of all available doctors.
Clears any existing content inside the #content div.
Iterates over the results and renders each doctor using createDoctorCard().
Appends each card to the #content section.
1
2
3
document.addEventListener("DOMContentLoaded", () => {
  loadDoctorCards();
});

Copied!

Wrap Toggled!
Bind Modal Triggers for Login and Signup
Add event listeners on:

The Signup button(#patientSignup) → opens the patientSignup modal.
The Login button(#patientLogin) → opens the patientLogin modal.
1
2
3
4
5
6
7
8
9
document.addEventListener("DOMContentLoaded", () => {
  const btn = document.getElementById("patientSignup");
  if (btn) btn.addEventListener("click", () => openModal("patientSignup"));
});
document.addEventListener("DOMContentLoaded", () => {
  const loginBtn = document.getElementById("patientLogin");
  if (loginBtn) loginBtn.addEventListener("click", () => openModal("patientLogin"));
});

Copied!

Wrap Toggled!
Search and Filter Logic
Set up listeners for:

Search bar(#searchBar)
Availability time filter(#filterTime)
Specialty filter(#filterSpecialty)
Each change triggers filterDoctorsOnChange().

1
2
3
document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange);
document.getElementById("filterTime").addEventListener("change", filterDoctorsOnChange);
document.getElementById("filterSpecialty").addEventListener("change", filterDoctorsOnChange);

Copied!

Wrap Toggled!
filterDoctorsOnChange() Function:
Gathers values from all three filter / search inputs.
Uses filterDoctors(name, time, specialty) to fetch filtered results.
Clears the existing content.
If doctors are found, renders them using createDoctorCard().
If not, displays a fallback message.
1
2
3
4
5
6
7
function filterDoctorsOnChange() {
  ...
  filterDoctors(name, time, specialty).then(response => {
    ...
    contentDiv.innerHTML = doctors.length > 0 ? renderedCards : "<p>No doctors found with the given filters.</p>";
});
}

Copied!

Wrap Toggled!
Render Utility
The renderDoctorCards(doctors) function is available for rendering a given list of doctors dynamically, used optionally by other modules.

Handle Patient Signup
The signupPatient() function is triggered on form submission:

Collects user inputs(name, email, password, phone, address).

Sends the data to the backend via patientSignup().

On success:

Shows an alert with a success message.
Closes the modal and reloads the page.
On failure: Shows an error message.

1
2
3
window.signupPatient = async function () {
  ...
};

Copied!

Wrap Toggled!
Handle Patient Login
The loginPatient() function is triggered on login form submission:

Captures login credentials(email, password).

Calls patientLogin() to authenticate.

On success:

Stores JWT token in localStorage.
Redirects user to loggedPatientDashboard.html.
On failure:

Shows error alert.
1
2
3
window.loginPatient = async function () {
  ...
};

Copied!

Wrap Toggled!
Notes
Ensure all modal IDs(patientSignup, patientLogin) are properly assigned in HTML.
All doctor fetching functions(getDoctors, filterDoctors) should be async / await -based for clean flow and error handling.
Use createDoctorCard() to maintain consistent UI design across dashboards.
Use fallback messages for empty search / filter results or API errors.
Save your work!
Before you finish this lab, make sure to add, commit, and push your updated code to your GitHub repository.You will be asked to provide your public GitHub repo URL for graded evaluation at the end of the capstone.

Follow these steps to push your changes:

Stage your changes:

1
git add.

  Copied!

Wrap Toggled!

Executed!
Commit your changes with a meaningful message:

1
git commit - m "Completed Developing Services and Utilities"

Copied!

Wrap Toggled!

Executed!
Push your changes using your GitHub Personal Access Token:

  1
git push https://<your_github_username>:<your_personal_access_token>@github.com/<your_github_username>/java-database-capstone.git

Copied!

Wrap Toggled!

Executed!
Replace < your_github_username > with your GitHub username.
  Replace < your_personal_access_token > with the token you generated at the start of the lab.
    Example
If your GitHub username is johnsmith and your token is ghp_ABC123xyzTOKEN, your push command would look like:

1
git push https://johnsmith:ghp_ABC123xyzTOKEN@github.com/johnsmith/java-database-capstone.git

Copied!

Wrap Toggled!

Executed!
Remember:

Always push your work after completing each lab.
Save your GitHub URL — you will need to submit it for your final project evaluation.


  // patientDashboard.js
  import { getDoctors } from './services/doctorServices.js';
import { openModal } from './components/modals.js';
import { createDoctorCard } from './components/doctorCard.js';
import { filterDoctors } from './services/doctorServices.js';//call the same function to avoid duplication coz the functionality was same
import { patientSignup, patientLogin } from './services/patientServices.js';



document.addEventListener("DOMContentLoaded", () => {
  loadDoctorCards();
});

document.addEventListener("DOMContentLoaded", () => {
  const btn = document.getElementById("patientSignup");
  if (btn) {
    btn.addEventListener("click", () => openModal("patientSignup"));
  }
});

document.addEventListener("DOMContentLoaded", () => {
  const loginBtn = document.getElementById("patientLogin")
  if (loginBtn) {
    loginBtn.addEventListener("click", () => {
      openModal("patientLogin")
    })
  }
})

function loadDoctorCards() {
  getDoctors()
    .then(doctors => {
      const contentDiv = document.getElementById("content");
      contentDiv.innerHTML = "";

      doctors.forEach(doctor => {
        const card = createDoctorCard(doctor);
        contentDiv.appendChild(card);
      });
    })
    .catch(error => {
      console.error("Failed to load doctors:", error);
    });
}
// Filter Input
document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange);
document.getElementById("filterTime").addEventListener("change", filterDoctorsOnChange);
document.getElementById("filterSpecialty").addEventListener("change", filterDoctorsOnChange);



function filterDoctorsOnChange() {
  const searchBar = document.getElementById("searchBar").value.trim();
  const filterTime = document.getElementById("filterTime").value;
  const filterSpecialty = document.getElementById("filterSpecialty").value;


  const name = searchBar.length > 0 ? searchBar : null;
  const time = filterTime.length > 0 ? filterTime : null;
  const specialty = filterSpecialty.length > 0 ? filterSpecialty : null;

  filterDoctors(name, time, specialty)
    .then(response => {
      const doctors = response.doctors;
      const contentDiv = document.getElementById("content");
      contentDiv.innerHTML = "";

      if (doctors.length > 0) {
        console.log(doctors);
        doctors.forEach(doctor => {
          const card = createDoctorCard(doctor);
          contentDiv.appendChild(card);
        });
      } else {
        contentDiv.innerHTML = "<p>No doctors found with the given filters.</p>";
        console.log("Nothing");
      }
    })
    .catch(error => {
      console.error("Failed to filter doctors:", error);
      alert("❌ An error occurred while filtering doctors.");
    });
}

window.signupPatient = async function () {
  try {
    const name = document.getElementById("name").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const phone = document.getElementById("phone").value;
    const address = document.getElementById("address").value;

    const data = { name, email, password, phone, address };
    const { success, message } = await patientSignup(data);
    if (success) {
      alert(message);
      document.getElementById("modal").style.display = "none";
      window.location.reload();
    }
    else alert(message);
  } catch (error) {
    console.error("Signup failed:", error);
    alert("❌ An error occurred while signing up.");
  }
};

window.loginPatient = async function () {
  try {
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    const data = {
      email,
      password
    }
    console.log("loginPatient :: ", data)
    const response = await patientLogin(data);
    console.log("Status Code:", response.status);
    console.log("Response OK:", response.ok);
    if (response.ok) {
      const result = await response.json();
      console.log(result);
      selectRole('loggedPatient');
      localStorage.setItem('token', result.token)
      window.location.href = '/pages/loggedPatientDashboard.html';
    } else {
      alert('❌ Invalid credentials!');
    }
  }
  catch (error) {
    alert("❌ Failed to Login : ", error);
    console.log("Error :: loginPatient :: ", error)
  }


}
