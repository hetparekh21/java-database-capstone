/*
  Import the openModal function to handle showing login popups/modals
  Import the base API URL from the config file
  Define constants for the admin and doctor login API endpoints using the base URL

  Use the window.onload event to ensure DOM elements are available after page load
  Inside this function:
    - Select the "adminLogin" and "doctorLogin" buttons using getElementById
    - If the admin login button exists:
        - Add a click event listener that calls openModal('adminLogin') to show the admin login modal
    - If the doctor login button exists:
        - Add a click event listener that calls openModal('doctorLogin') to show the doctor login modal


  Define a function named adminLoginHandler on the global window object
  This function will be triggered when the admin submits their login credentials

  Step 1: Get the entered username and password from the input fields
  Step 2: Create an admin object with these credentials

  Step 3: Use fetch() to send a POST request to the ADMIN_API endpoint
    - Set method to POST
    - Add headers with 'Content-Type: application/json'
    - Convert the admin object to JSON and send in the body

  Step 4: If the response is successful:
    - Parse the JSON response to get the token
    - Store the token in localStorage
    - Call selectRole('admin') to proceed with admin-specific behavior

  Step 5: If login fails or credentials are invalid:
    - Show an alert with an error message

  Step 6: Wrap everything in a try-catch to handle network or server errors
    - Show a generic error message if something goes wrong


  Define a function named doctorLoginHandler on the global window object
  This function will be triggered when a doctor submits their login credentials

  Step 1: Get the entered email and password from the input fields
  Step 2: Create a doctor object with these credentials

  Step 3: Use fetch() to send a POST request to the DOCTOR_API endpoint
    - Include headers and request body similar to admin login

  Step 4: If login is successful:
    - Parse the JSON response to get the token
    - Store the token in localStorage
    - Call selectRole('doctor') to proceed with doctor-specific behavior

  Step 5: If login fails:
    - Show an alert for invalid credentials

  Step 6: Wrap in a try-catch block to handle errors gracefully
    - Log the error to the console
    - Show a generic error message
*/

import { openModal } from '../components/modals.js';
import { API_BASE_URL } from '../config/config.js';
import { patientSignup, patientLogin } from './patientServices.js';

const ADMIN_API = `${API_BASE_URL}/admin/login`;
const DOCTOR_API = `${API_BASE_URL}/doctor/login`;

window.addEventListener('load', () => {
  // role selector buttons on the landing page
  const adminBtn = document.getElementById('adminBtn') || document.getElementById('adminLogin');
  const patientBtn = document.getElementById('patientBtn') || document.getElementById('patientLogin')
  const doctorBtn = document.getElementById('doctorBtn') || document.getElementById('doctorLogin');

  if (adminBtn) {
    adminBtn.addEventListener('click', () => {
      try {
        // prefer the imported function, but fall back to a global if present
        if (typeof openModal === 'function') return openModal('adminLogin');
        if (typeof window.openModal === 'function') return window.openModal('adminLogin');
        import('../components/modals.js').then(m => m.openModal('adminLogin')).catch(() => alert('Login modal not available'));
      } catch (e) {
        console.error('Could not open admin login modal', e);
      }
    });
  }

  if (patientBtn) {
    patientBtn.addEventListener('click', () => {
      try {
        // prefer the imported function, but fall back to a global if present
        if (typeof openModal === 'function') return openModal('patientLogin');
        if (typeof window.openModal === 'function') return window.openModal('patientLogin');
        import('../components/modals.js').then(m => m.openModal('patientLogin')).catch(() => alert('Login modal not available'));
      } catch (e) {
        console.error('Could not open patient login modal', e);
      }
    });
  }

  if (doctorBtn) {
    doctorBtn.addEventListener('click', () => {
      try {
        if (typeof openModal === 'function') return openModal('doctorLogin');
        if (typeof window.openModal === 'function') return window.openModal('doctorLogin');
        import('../components/modals.js').then(m => m.openModal('doctorLogin')).catch(() => alert('Login modal not available'));
      } catch (e) {
        console.error('Could not open doctor login modal', e);
      }
    });
  }
});

// Admin login handler called from the modal (modals.js wires adminLoginHandler)
window.adminLoginHandler = async function adminLoginHandler() {
  try {
    const username = document.getElementById('username')?.value?.trim();
    const password = document.getElementById('password')?.value;
    if (!username || !password) {
      alert('Please enter username and password');
      return;
    }

    const payload = { username, password };
    const res = await fetch(ADMIN_API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      let body = null;
      try { body = await res.json(); } catch (e) { /* ignore */ }
      alert(body?.message || 'Admin login failed');
      return;
    }

    const data = await res.json();
    const token = data?.token;
    if (!token) return alert('No token received from server');

    localStorage.setItem('token', token);
    localStorage.setItem('role', 'admin');
    // proceed to admin area
    if (typeof window.selectRole === 'function') return window.selectRole('admin');
    // fallback: navigate to admin dashboard with token
    window.location.href = `/adminDashboard/${token}`;
  } catch (err) {
    console.error('adminLoginHandler error', err);
    alert('An error occurred while logging in. Please try again.');
  }
};

// Doctor login handler called from the modal (modals.js wires doctorLoginHandler)
window.doctorLoginHandler = async function doctorLoginHandler() {
  try {
    const email = document.getElementById('email')?.value?.trim();
    const password = document.getElementById('password')?.value;
    if (!email || !password) {
      alert('Please enter email and password');
      return;
    }

    const payload = { email, password };
    const res = await fetch(DOCTOR_API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      let body = null;
      try { body = await res.json(); } catch (e) { /* ignore */ }
      alert(body?.message || 'Doctor login failed');
      return;
    }

    const data = await res.json();
    const token = data?.token;
    if (!token) return alert('No token received from server');

    localStorage.setItem('token', token);
    localStorage.setItem('role', 'doctor');
    if (typeof window.selectRole === 'function') return window.selectRole('doctor');
    window.location.href = `/doctorDashboard/${token}`;
  } catch (err) {
    console.error('doctorLoginHandler error', err);
    alert('An error occurred while logging in. Please try again.');
  }
};

// Patient login handler called from the modal (modals.js wires patientLoginHandler)
window.loginPatient = async function loginPatient() {
  try {
    const email = document.getElementById('email')?.value?.trim();
    const password = document.getElementById('password')?.value;
    if (!email || !password) {
      alert('Please enter email and password');
      return;
    }

    const res = await patientLogin({ email, password });
    if (!res.ok) {
      let body = null;
      try { body = await res.json(); } catch (e) { /* ignore */ }
      alert(body?.message || 'Patient login failed');
      return;
    }
    const data = await res.json();
    const token = data?.token;
    if (!token) return alert('No token received from server');

    localStorage.setItem('token', token);
    localStorage.setItem('role', 'patient');
    if (typeof window.selectRole === 'function') return window.selectRole('patient');
    window.location.href = `/pages/patientDashboard.html`;
  } catch (err) {
    console.error('loginPatient error', err);
    alert('An error occurred while logging in. Please try again.');
  }
};

// Patient signup handler called from the modal (modals.js wires signupPatient)
window.signupPatient = async function signupPatient() {
  try {
    const name = document.getElementById('name')?.value?.trim();
    const email = document.getElementById('email')?.value?.trim();
    const password = document.getElementById('password')?.value;
    const phone = document.getElementById('phone')?.value?.trim();
    const address = document.getElementById('address')?.value?.trim();

    if (!name || !email || !password || !phone) {
      alert('Please fill all required fields');
      return;
    }

    const payload = { name, email, password, phone, address };
    const result = await patientSignup(payload);
    if (result.success) {
      alert(result.message || 'Signup successful. Please login.');
      // close modal if global function exists
      if (document.getElementById('closeModal')) document.getElementById('closeModal').click();
    } else {
      alert(result.message || 'Signup failed');
    }
  } catch (err) {
    console.error('signupPatient error', err);
    alert('An error occurred while signing up. Please try again.');
  }
};

export {};

