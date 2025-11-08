/*
Import the overlay function for booking appointments from loggedPatient.js

  Import the deleteDoctor API function to remove doctors (admin role) from docotrServices.js

  Import function to fetch patient details (used during booking) from patientServices.js

  Function to create and return a DOM element for a single doctor card
    Create the main container for the doctor card
    Retrieve the current user role from localStorage
    Create a div to hold doctor information
    Create and set the doctor’s name
    Create and set the doctor's specialization
    Create and set the doctor's email
    Create and list available appointment times
    Append all info elements to the doctor info container
    Create a container for card action buttons
    === ADMIN ROLE ACTIONS ===
      Create a delete button
      Add click handler for delete button
     Get the admin token from localStorage
        Call API to delete the doctor
        Show result and remove card if successful
      Add delete button to actions container
   
    === PATIENT (NOT LOGGED-IN) ROLE ACTIONS ===
      Create a book now button
      Alert patient to log in before booking
      Add button to actions container
  
    === LOGGED-IN PATIENT ROLE ACTIONS === 
      Create a book now button
      Handle booking logic for logged-in patient   
        Redirect if token not available
        Fetch patient data with token
        Show booking overlay UI with doctor and patient info
      Add button to actions container
   
  Append doctor info and action buttons to the car
  Return the complete doctor card element
*/

import { deleteDoctor } from '../services/doctorServices.js';
import { getPatientData } from '../services/patientServices.js';
import { showBookingOverlay } from '../loggedPatient.js';

/**
 * createDoctorCard
 * @param {Object} doctor - doctor object expected to have: id, name, specialty, email, availableTimes (array)
 * @returns {HTMLElement} card element
 */
export function createDoctorCard(doctor) {
  const role = localStorage.getItem('userRole');

  const card = document.createElement('div');
  card.className = 'doctor-card';

  // Info container
  const info = document.createElement('div');
  info.className = 'doctor-info';

  const name = document.createElement('h3');
  name.textContent = doctor.name || 'Unknown Doctor';

  const specialty = document.createElement('p');
  specialty.textContent = doctor.specialty || '';

  const email = document.createElement('p');
  email.textContent = doctor.email || '';

  const timesWrapper = document.createElement('div');
  timesWrapper.className = 'available-times';
  if (Array.isArray(doctor.availableTimes) && doctor.availableTimes.length) {
    const ul = document.createElement('ul');
    doctor.availableTimes.forEach(t => {
      const li = document.createElement('li');
      li.textContent = t;
      ul.appendChild(li);
    });
    timesWrapper.appendChild(ul);
  } else {
    timesWrapper.textContent = 'No available times';
  }

  info.appendChild(name);
  info.appendChild(specialty);
  info.appendChild(email);
  info.appendChild(timesWrapper);

  // Actions container
  const actions = document.createElement('div');
  actions.className = 'card-actions';

  // ADMIN actions
  if (role === 'admin') {
    const delBtn = document.createElement('button');
    delBtn.className = 'adminBtn';
    delBtn.textContent = 'Delete';
    delBtn.addEventListener('click', async () => {
      const token = localStorage.getItem('token');
      if (!token) {
        alert('Admin token not found. Please login.');
        return;
      }
      if (!confirm('Are you sure you want to delete this doctor?')) return;
      const { success, message } = await deleteDoctor(doctor.id, token);
      if (success) {
        alert('Doctor deleted');
        card.remove();
      } else {
        alert('Failed to delete doctor: ' + message);
      }
    });
    actions.appendChild(delBtn);
  }

  // PATIENT (not logged in) actions
  if (role === 'patient' || !role) {
    const bookBtn = document.createElement('button');
    bookBtn.className = 'adminBtn';
    bookBtn.textContent = 'Book Now';
    bookBtn.addEventListener('click', () => {
      // If patient role but not logged in, prompt to login/signup
      const token = localStorage.getItem('token');
      if (!token) {
        // try to open patient login modal if available globally
        if (typeof window.openModal === 'function') {
          window.openModal('patientLogin');
        } else {
          alert('Please login as a patient to book an appointment.');
        }
        return;
      }
      // If token exists, proceed to logged-patient flow below
      // we intentionally fallthrough to loggedPatient handling by calling same handler
      (async () => {
        const patient = await getPatientData(localStorage.getItem('token'));
        if (!patient) {
          alert('Unable to fetch patient data. Please login again.');
          return;
        }
        showBookingOverlay({ target: bookBtn, clientX: 0, clientY: 0 }, doctor, patient);
      })();
    });
    actions.appendChild(bookBtn);
  }

  // LOGGED-IN PATIENT actions
  if (role === 'loggedPatient') {
    const bookBtn = document.createElement('button');
    bookBtn.className = 'adminBtn';
    bookBtn.textContent = 'Book Now';
    bookBtn.addEventListener('click', async (e) => {
      const token = localStorage.getItem('token');
      if (!token) {
        alert('Session expired or not logged in.');
        window.location.href = '/pages/patientDashboard.html';
        return;
      }
      const patient = await getPatientData(token);
      if (!patient) {
        alert('Unable to fetch patient data.');
        return;
      }
      // show booking overlay from loggedPatient module
      showBookingOverlay(e, doctor, patient);
    });
    actions.appendChild(bookBtn);
  }

  card.appendChild(info);
  card.appendChild(actions);

  return card;
}

