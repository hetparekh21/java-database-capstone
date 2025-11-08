/*
  Step-by-Step Explanation of Header Section Rendering

  This code dynamically renders the header section of the page based on the user's role, session status, and available actions (such as login, logout, or role-switching).

  1. Define the `renderHeader` Function

     * The `renderHeader` function is responsible for rendering the entire header based on the user's session, role, and whether they are logged in.

  2. Select the Header Div

     * The `headerDiv` variable retrieves the HTML element with the ID `header`, where the header content will be inserted.
       ```javascript
       const headerDiv = document.getElementById("header");
       ```

  3. Check if the Current Page is the Root Page

     * The `window.location.pathname` is checked to see if the current page is the root (`/`). If true, the user's session data (role) is removed from `localStorage`, and the header is rendered without any user-specific elements (just the logo and site title).
       ```javascript
       if (window.location.pathname.endsWith("/")) {
         localStorage.removeItem("userRole");
         headerDiv.innerHTML = `
           <header class="header">
             <div class="logo-section">
               <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
               <span class="logo-title">Hospital CMS</span>
             </div>
           </header>`;
         return;
       }
       ```

  4. Retrieve the User's Role and Token from LocalStorage

     * The `role` (user role like admin, patient, doctor) and `token` (authentication token) are retrieved from `localStorage` to determine the user's current session.
       ```javascript
       const role = localStorage.getItem("userRole");
       const token = localStorage.getItem("token");
       ```

  5. Initialize Header Content

     * The `headerContent` variable is initialized with basic header HTML (logo section), to which additional elements will be added based on the user's role.
       ```javascript
       let headerContent = `<header class="header">
         <div class="logo-section">
           <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
           <span class="logo-title">Hospital CMS</span>
         </div>
         <nav>`;
       ```

  6. Handle Session Expiry or Invalid Login

     * If a user with a role like `loggedPatient`, `admin`, or `doctor` does not have a valid `token`, the session is considered expired or invalid. The user is logged out, and a message is shown.
       ```javascript
       if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
         localStorage.removeItem("userRole");
         alert("Session expired or invalid login. Please log in again.");
         window.location.href = "/";   or a specific login page
         return;
       }
       ```

  7. Add Role-Specific Header Content

     * Depending on the user's role, different actions or buttons are rendered in the header:
       - **Admin**: Can add a doctor and log out.
       - **Doctor**: Has a home button and log out.
       - **Patient**: Shows login and signup buttons.
       - **LoggedPatient**: Has home, appointments, and logout options.
       ```javascript
       else if (role === "admin") {
         headerContent += `
           <button id="addDocBtn" class="adminBtn" onclick="openModal('addDoctor')">Add Doctor</button>
           <a href="#" onclick="logout()">Logout</a>`;
       } else if (role === "doctor") {
         headerContent += `
           <button class="adminBtn"  onclick="selectRole('doctor')">Home</button>
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
       ```



  9. Close the Header Section


/* header.js
   Implements the header rendering and behavior described in the original comment block.
   This file is kept as a classic script (not a module) because pages include it via a normal <script> tag.

   Behavior provided:
   - renderHeader(): builds header DOM based on localStorage.userRole and localStorage.token
   - attachHeaderButtonListeners(): wires buttons (login/signup/add doctor/home/appointments/logout)
   - logout() and logoutPatient(): clear session and redirect
   - Attempts to open modal UI by calling window.openModal (if available) or by dynamically importing
     the modals module from a known path (/js/components/modals.js).
*/

(function () {
  'use strict';

  function getHeaderDiv() {
    return document.getElementById('header');
  }

  function isRootPath() {
    // Treat '/' or empty pathname as root. Also make sure trailing slash is handled.
    const p = window.location.pathname || '/';
    return p === '/' || p === '' || p.endsWith('/index.html');
  }

  async function tryOpenModal(type) {
    // Prefer global openModal if present (some pages import modals as module and expose it)
    if (typeof window.openModal === 'function') {
      try {
        window.openModal(type);
        return;
      } catch (e) {
        console.warn('window.openModal failed:', e);
      }
    }

    // Otherwise try dynamic import from the canonical location where components live.
    // This should work in modern browsers: the module exports openModal.
    try {
      const mod = await import('/js/components/modals.js');
      if (mod && typeof mod.openModal === 'function') {
        mod.openModal(type);
        return;
      }
    } catch (e) {
      // Ignore — fallback below
      console.warn('dynamic import of modals.js failed:', e);
    }

    // Fallback: try to render minimal modal content directly (for pages that don't include the module).
    const modal = document.getElementById('modal');
    const body = document.getElementById('modal-body');
    if (!modal || !body) {
      alert('Modal is not available on this page.');
      return;
    }

    // Very small fallback UI for login/signup that mirrors the original modals where sensible.
    if (type === 'patientLogin') {
      body.innerHTML = `
        <h2>Patient Login</h2>
        <input type="text" id="email" placeholder="Email" class="input-field">
        <input type="password" id="password" placeholder="Password" class="input-field">
        <button class="dashboard-btn" id="loginBtn">Login</button>
      `;
      modal.style.display = 'block';
      document.getElementById('closeModal').onclick = () => { modal.style.display = 'none'; };
      // If a global loginPatient handler exists, wire it.
      const loginBtn = document.getElementById('loginBtn');
      if (loginBtn && typeof window.loginPatient === 'function') {
        loginBtn.addEventListener('click', window.loginPatient);
      }
      return;
    }

    if (type === 'patientSignup') {
      body.innerHTML = `
        <h2>Patient Signup</h2>
        <input type="text" id="name" placeholder="Name" class="input-field">
        <input type="email" id="email" placeholder="Email" class="input-field">
        <input type="password" id="password" placeholder="Password" class="input-field">
        <input type="text" id="phone" placeholder="Phone" class="input-field">
        <input type="text" id="address" placeholder="Address" class="input-field">
        <button class="dashboard-btn" id="signupBtn">Signup</button>
      `;
      modal.style.display = 'block';
      document.getElementById('closeModal').onclick = () => { modal.style.display = 'none'; };
      if (document.getElementById('signupBtn') && typeof window.signupPatient === 'function') {
        document.getElementById('signupBtn').addEventListener('click', window.signupPatient);
      }
      return;
    }

    // For addDoctor/adminLogin/doctorLogin we'll attempt to load modals via dynamic import only.
    alert('This action requires the modals module. Please ensure the page includes /js/components/modals.js');
  }

  function logout() {
    localStorage.removeItem('userRole');
    localStorage.removeItem('token');
    // Redirect to site root
    window.location.href = '/';
  }

  function logoutPatient() {
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');
    // Send the user to the public patient dashboard
    window.location.href = '/pages/patientDashboard.html';
  }

  function buildLogoSection() {
    return `
      <div class="logo-section">
        <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
        <span class="logo-title">Hospital CMS</span>
      </div>`;
  }

  function renderHeader() {
    const headerDiv = getHeaderDiv();
    if (!headerDiv) return;

    // If on root page, clear any stored role and render minimal header
    if (isRootPath()) {
      localStorage.removeItem('userRole');
      headerDiv.innerHTML = `<header class="header">${buildLogoSection()}</header>`;
      return;
    }

    const role = localStorage.getItem('userRole');
    const token = localStorage.getItem('token');

    // If role indicates an authenticated section but token missing => expire session
    if ((role === 'loggedPatient' || role === 'admin' || role === 'doctor') && !token) {
      localStorage.removeItem('userRole');
      alert('Session expired or invalid login. Please log in again.');
      window.location.href = '/';
      return;
    }

    let headerContent = `<header class="header">${buildLogoSection()}<nav class="nav-actions">`;

    if (role === 'admin') {
      headerContent += `
        <button id="addDocBtn" class="adminBtn">Add Doctor</button>
        <a href="#" id="logoutLink">Logout</a>`;
    } else if (role === 'doctor') {
      headerContent += `
        <button id="doctorHomeBtn" class="adminBtn">Home</button>
        <a href="#" id="logoutLink">Logout</a>`;
    } else if (role === 'patient') {
      headerContent += `
        <button id="patientLogin" class="adminBtn">Login</button>
        <button id="patientSignup" class="adminBtn">Sign Up</button>`;
    } else if (role === 'loggedPatient') {
      headerContent += `
        <button id="home" class="adminBtn">Home</button>
        <button id="patientAppointments" class="adminBtn">Appointments</button>
        <a href="#" id="logoutPatientLink">Logout</a>`;
    } else {
      // No role stored: show links to choose a role or go to public pages
      headerContent += `
        <a href="/" class="adminBtn">Role Selection</a>`;
    }

    headerContent += `</nav></header>`;

    headerDiv.innerHTML = headerContent;

    attachHeaderButtonListeners();
  }

  function attachHeaderButtonListeners() {
    // Add Doctor (admin)
    const addDocBtn = document.getElementById('addDocBtn');
    if (addDocBtn) {
      addDocBtn.addEventListener('click', () => tryOpenModal('addDoctor'));
    }

    // Doctor Home
    const doctorHomeBtn = document.getElementById('doctorHomeBtn');
    if (doctorHomeBtn) {
      doctorHomeBtn.addEventListener('click', () => selectRole && selectRole('doctor'));
    }

    // Patient Login / Signup
    const patientLoginBtn = document.getElementById('patientLogin');
    if (patientLoginBtn) {
      patientLoginBtn.addEventListener('click', () => tryOpenModal('patientLogin'));
    }

    const patientSignupBtn = document.getElementById('patientSignup');
    if (patientSignupBtn) {
      patientSignupBtn.addEventListener('click', () => tryOpenModal('patientSignup'));
    }

    // Logged patient buttons
    const homeBtn = document.getElementById('home');
    if (homeBtn) {
      homeBtn.addEventListener('click', () => { window.location.href = '/pages/loggedPatientDashboard.html'; });
    }

    const patientAppointmentsBtn = document.getElementById('patientAppointments');
    if (patientAppointmentsBtn) {
      patientAppointmentsBtn.addEventListener('click', () => { window.location.href = '/pages/patientAppointments.html'; });
    }

    // Logout links
    const logoutLink = document.getElementById('logoutLink');
    if (logoutLink) {
      logoutLink.addEventListener('click', (e) => { e.preventDefault(); logout(); });
    }

    const logoutPatientLink = document.getElementById('logoutPatientLink');
    if (logoutPatientLink) {
      logoutPatientLink.addEventListener('click', (e) => { e.preventDefault(); logoutPatient(); });
    }
  }

  // Expose functions globally so templates and other scripts can call them if needed
  window.renderHeader = renderHeader;
  window.attachHeaderButtonListeners = attachHeaderButtonListeners;
  window.logout = logout;
  window.logoutPatient = logoutPatient;

  // Render immediately (header.js is loaded with defer in pages)
  document.addEventListener('DOMContentLoaded', renderHeader);

})();



