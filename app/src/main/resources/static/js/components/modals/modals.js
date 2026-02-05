// modals.js
// SAFETY CHECK - Add this at the VERY TOP of the file
console.log("Loading modals.js");

// Wait a bit for services to load, then define placeholders if needed
setTimeout(() => {
  if (typeof adminLoginHandler === 'undefined') {
    console.warn('adminLoginHandler not loaded, defining emergency handler');
    window.adminLoginHandler = function () {
      console.log('Emergency admin login handler');
      const username = document.getElementById('username')?.value;
      const password = document.getElementById('password')?.value;

      if (username && password) {
        fetch('http://localhost:8080/admin/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username, password })
        })
          .then(response => response.json())
          .then(data => {
            if (data.token) {
              localStorage.setItem('token', data.token);
              localStorage.setItem('userRole', 'admin');
              document.getElementById('modal').style.display = 'none';
              window.location.href = '/adminDashboard';
            } else {
              alert('Login failed: ' + (data.message || 'Invalid credentials'));
            }
          })
          .catch(err => {
            console.error('Login error:', err);
            alert('Login failed. Please try again.');
          });
      }
    };
  }

  if (typeof doctorLoginHandler === 'undefined') {
    window.doctorLoginHandler = function () {
      alert('Doctor login - services still loading. Try again in a moment.');
    };
  }

  if (typeof loginPatient === 'undefined') {
    window.loginPatient = function () {
      alert('Patient login - services still loading. Try again in a moment.');
    };
  }
}, 100);

// KEEP THE REST OF YOUR EXISTING openModal FUNCTION EXACTLY AS IS
function openModal(type) {
  let modalContent = '';
  if (type === 'addDoctor') {
    modalContent = `
         <h2>Add Doctor</h2>
         <input type="text" id="doctorName" placeholder="Doctor Name" class="input-field">
         <select id="specialization" class="input-field select-dropdown">
             <option value="">Specialization</option>
                        <option value="cardiologist">Cardiologist</option>
                        <option value="dermatologist">Dermatologist</option>
                        <option value="neurologist">Neurologist</option>
                        <option value="pediatrician">Pediatrician</option>
                        <option value="orthopedic">Orthopedic</option>
                        <option value="gynecologist">Gynecologist</option>
                        <option value="psychiatrist">Psychiatrist</option>
                        <option value="dentist">Dentist</option>
                        <option value="ophthalmologist">Ophthalmologist</option>
                        <option value="ent">ENT Specialist</option>
                        <option value="urologist">Urologist</option>
                        <option value="oncologist">Oncologist</option>
                        <option value="gastroenterologist">Gastroenterologist</option>
                        <option value="general">General Physician</option>
        </select>
        <input type="email" id="doctorEmail" placeholder="Email" class="input-field">
        <input type="password" id="doctorPassword" placeholder="Password" class="input-field">
        <input type="text" id="doctorPhone" placeholder="Mobile No." class="input-field">
        <div class="availability-container">
        <label class="availabilityLabel">Select Availability:</label>
          <div class="checkbox-group">
              <label><input type="checkbox" name="availability" value="09:00-10:00"> 9:00 AM - 10:00 AM</label>
              <label><input type="checkbox" name="availability" value="10:00-11:00"> 10:00 AM - 11:00 AM</label>
              <label><input type="checkbox" name="availability" value="11:00-12:00"> 11:00 AM - 12:00 PM</label>
              <label><input type="checkbox" name="availability" value="12:00-13:00"> 12:00 PM - 1:00 PM</label>
          </div>
        </div>
        <button class="dashboard-btn" id="saveDoctorBtn">Save</button>
      `;
  } else if (type === 'patientLogin') {
    modalContent = `
        <h2>Patient Login</h2>
        <input type="text" id="email" placeholder="Email" class="input-field">
        <input type="password" id="password" placeholder="Password" class="input-field">
        <button class="dashboard-btn" id="loginBtn">Login</button>
      `;
  }
  else if (type === "patientSignup") {
    modalContent = `
      <h2>Patient Signup</h2>
      <input type="text" id="name" placeholder="Name" class="input-field">
      <input type="email" id="email" placeholder="Email" class="input-field">
      <input type="password" id="password" placeholder="Password" class="input-field">
      <input type="text" id="phone" placeholder="Phone" class="input-field">
      <input type="text" id="address" placeholder="Address" class="input-field">
      <button class="dashboard-btn" id="signupBtn">Signup</button>
    `;

  } else if (type === 'adminLogin') {
    modalContent = `
        <h2>Admin Login</h2>
        <input type="text" id="username" name="username" placeholder="Username" class="input-field">
        <input type="password" id="password" name="password" placeholder="Password" class="input-field">
        <button class="dashboard-btn" id="adminLoginBtn" >Login</button>
      `;
  } else if (type === 'doctorLogin') {
    modalContent = `
        <h2>Doctor Login</h2>
        <input type="text" id="email" placeholder="Email" class="input-field">
        <input type="password" id="password" placeholder="Password" class="input-field">
        <button class="dashboard-btn" id="doctorLoginBtn" >Login</button>
      `;
  }

  document.getElementById('modal-body').innerHTML = modalContent;
  document.getElementById('modal').style.display = 'block';

  document.getElementById('closeModal').onclick = () => {
    document.getElementById('modal').style.display = 'none';
  };

  if (type === "patientSignup") {
    document.getElementById("signupBtn").addEventListener("click", signupPatient);
  }

  if (type === "patientLogin") {
    document.getElementById("loginBtn").addEventListener("click", loginPatient);
  }

  if (type === 'addDoctor') {
    document.getElementById('saveDoctorBtn').addEventListener('click', adminAddDoctor);
  }

  if (type === 'adminLogin') {
    document.getElementById('adminLoginBtn').addEventListener('click', adminLoginHandler);
  }

  if (type === 'doctorLogin') {
    document.getElementById('doctorLoginBtn').addEventListener('click', doctorLoginHandler);
  }
}

window.openModal = openModal;
console.log("Modal system ready");