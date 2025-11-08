/*
  This script handles the admin dashboard functionality for managing doctors:
  - Loads all doctor cards
  - Filters doctors by name, time, or specialty
  - Adds a new doctor via modal form


  Attach a click listener to the "Add Doctor" button
  When clicked, it opens a modal form using openModal('addDoctor')


  When the DOM is fully loaded:
    - Call loadDoctorCards() to fetch and display all doctors


  Function: loadDoctorCards
  Purpose: Fetch all doctors and display them as cards

    Call getDoctors() from the service layer
    Clear the current content area
    For each doctor returned:
    - Create a doctor card using createDoctorCard()
    - Append it to the content div

    Handle any fetch errors by logging them


  Attach 'input' and 'change' event listeners to the search bar and filter dropdowns
  On any input change, call filterDoctorsOnChange()


  Function: filterDoctorsOnChange
  Purpose: Filter doctors based on name, available time, and specialty

    Read values from the search bar and filters
    Normalize empty values to null
    Call filterDoctors(name, time, specialty) from the service

    If doctors are found:
    - Render them using createDoctorCard()
    If no doctors match the filter:
    - Show a message: "No doctors found with the given filters."

    Catch and display any errors with an alert


  Function: renderDoctorCards
  Purpose: A helper function to render a list of doctors passed to it

    Clear the content area
    Loop through the doctors and append each card to the content area


  Function: adminAddDoctor
  Purpose: Collect form data and add a new doctor to the system

    Collect input values from the modal form
    - Includes name, email, phone, password, specialty, and available times

    Retrieve the authentication token from localStorage
    - If no token is found, show an alert and stop execution

    Build a doctor object with the form values

    Call saveDoctor(doctor, token) from the service

    If save is successful:
    - Show a success message
    - Close the modal and reload the page

    If saving fails, show an error message
*/

import { getDoctors, filterDoctors, saveDoctor } from './services/doctorServices.js';
import { createDoctorCard } from './components/doctorCard.js';

// Load and render all doctors when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
  loadDoctorCards();

  const search = document.getElementById('searchBar');
  const filterTime = document.getElementById('filterTime');
  const filterSpecialty = document.getElementById('filterSpecialty');

  if (search) search.addEventListener('input', filterDoctorsOnChange);
  if (filterTime) filterTime.addEventListener('change', filterDoctorsOnChange);
  if (filterSpecialty) filterSpecialty.addEventListener('change', filterDoctorsOnChange);

  // If there's an Add Doctor button on the page (in header or elsewhere), ensure it opens modal
  const addDocBtn = document.getElementById('addDocBtn');
  if (addDocBtn) {
    addDocBtn.addEventListener('click', () => {
      if (typeof window.openModal === 'function') return window.openModal('addDoctor');
      import('/js/components/modals.js').then(m => m.openModal('addDoctor')).catch(() => alert('Add doctor modal not available'));
    });
  }
});

export async function loadDoctorCards() {
  try {
    const doctors = await getDoctors();
    renderDoctorCards(doctors);
  } catch (error) {
    console.error('Failed to load doctors:', error);
  }
}

export function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById('content');
  if (!contentDiv) return;
  contentDiv.innerHTML = '';
  if (!Array.isArray(doctors) || doctors.length === 0) {
    contentDiv.innerHTML = '<p>No doctors available.</p>';
    return;
  }
  doctors.forEach(doctor => {
    const card = createDoctorCard(doctor);
    contentDiv.appendChild(card);
  });
}

export function filterDoctorsOnChange() {
  const searchBar = document.getElementById('searchBar')?.value.trim() || '';
  const filterTime = document.getElementById('filterTime')?.value || '';
  const filterSpecialty = document.getElementById('filterSpecialty')?.value || '';

  const name = searchBar.length > 0 ? searchBar : null;
  const time = filterTime.length > 0 ? filterTime : null;
  const specialty = filterSpecialty.length > 0 ? filterSpecialty : null;

  filterDoctors(name, time, specialty)
    .then(response => {
      const doctors = response.doctors || [];
      if (doctors.length > 0) {
        renderDoctorCards(doctors);
      } else {
        const contentDiv = document.getElementById('content');
        if (contentDiv) contentDiv.innerHTML = '<p>No doctors found with the given filters.</p>';
      }
    })
    .catch(error => {
      console.error('Failed to filter doctors:', error);
      alert('❌ An error occurred while filtering doctors.');
    });
}

// Admin add doctor handler (wired by modals.js saveDoctorBtn)
window.adminAddDoctor = async function () {
  try {
    const name = document.getElementById('doctorName')?.value || '';
    const specialty = document.getElementById('specialization')?.value || '';
    const email = document.getElementById('doctorEmail')?.value || '';
    const password = document.getElementById('doctorPassword')?.value || '';
    const phone = document.getElementById('doctorPhone')?.value || '';

    // collect availability checkboxes
    const availNodes = Array.from(document.querySelectorAll('input[name="availability"]'));
    const availableTimes = availNodes.filter(n => n.checked).map(n => n.value);

    const token = localStorage.getItem('token');
    if (!token) {
      alert('Admin token not found. Please log in as admin.');
      return;
    }

    const doctor = {
      name,
      specialty,
      email,
      password,
      phone,
      availableTimes
    };

    const { success, message } = await saveDoctor(doctor, token);
    if (success) {
      alert('Doctor added successfully');
      // close modal if present
      const modal = document.getElementById('modal');
      if (modal) modal.style.display = 'none';
      // reload the doctor list
      await loadDoctorCards();
    } else {
      alert('Failed to add doctor: ' + message);
    }
  } catch (error) {
    console.error('adminAddDoctor error', error);
    alert('An error occurred while adding doctor.');
  }
};

