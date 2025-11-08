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

import { API_BASE_URL } from '../config/config.js';

const ADMIN_LOGIN_API = `${API_BASE_URL}/admin/login`;
const DOCTOR_LOGIN_API = `${API_BASE_URL}/doctor/login`;

// If pages provide buttons that open the login modals, wire them to call openModal when available
window.addEventListener('load', () => {
  try {
    const adminBtn = document.getElementById('adminLogin');
    const doctorBtn = document.getElementById('doctorLogin');
    if (adminBtn) {
      adminBtn.addEventListener('click', () => {
        if (typeof window.openModal === 'function') return window.openModal('adminLogin');
        // try dynamic import fallback
        import('/js/components/modals.js').then(mod => mod.openModal('adminLogin')).catch(() => {
          alert('Login modal is not available on this page.');
        });
      });
    }

    if (doctorBtn) {
      doctorBtn.addEventListener('click', () => {
        if (typeof window.openModal === 'function') return window.openModal('doctorLogin');
        import('/js/components/modals.js').then(mod => mod.openModal('doctorLogin')).catch(() => {
          alert('Login modal is not available on this page.');
        });
      });
    }
  } catch (e) {
    console.warn('Error wiring login modal buttons', e);
  }
});

// Admin login handler used by the admin login modal
window.adminLoginHandler = async function () {
  try {
    const username = document.getElementById('username')?.value || '';
    const password = document.getElementById('password')?.value || '';

    const payload = { username, password };

    const res = await fetch(ADMIN_LOGIN_API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      alert(err.message || 'Invalid admin credentials');
      return;
    }

    const data = await res.json();
    const token = data.token || data.data?.token || null;
    if (!token) {
      alert('Login succeeded but token not returned by server.');
      return;
    }

    localStorage.setItem('token', token);
    localStorage.setItem('userRole', 'admin');
    if (typeof selectRole === 'function') {
      selectRole('admin');
    } else {
      window.location.href = `/adminDashboard/${token}`;
    }
  } catch (error) {
    console.error('adminLoginHandler error', error);
    alert('An error occurred during admin login.');
  }
};

// Doctor login handler used by the doctor login modal
window.doctorLoginHandler = async function () {
  try {
    const email = document.getElementById('email')?.value || '';
    const password = document.getElementById('password')?.value || '';
    const payload = { email, password };

    const res = await fetch(DOCTOR_LOGIN_API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      alert(err.message || 'Invalid doctor credentials');
      return;
    }

    const data = await res.json();
    const token = data.token || data.data?.token || null;
    if (!token) {
      alert('Login succeeded but token not returned by server.');
      return;
    }

    localStorage.setItem('token', token);
    localStorage.setItem('userRole', 'doctor');
    if (typeof selectRole === 'function') {
      selectRole('doctor');
    } else {
      window.location.href = `/doctorDashboard/${token}`;
    }
  } catch (error) {
    console.error('doctorLoginHandler error', error);
    alert('An error occurred during doctor login.');
  }
};


