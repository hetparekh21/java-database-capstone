/*
  Import getAllAppointments to fetch appointments from the backend
  Import createPatientRow to generate a table row for each patient appointment


  Get the table body where patient rows will be added
  Initialize selectedDate with today's date in 'YYYY-MM-DD' format
  Get the saved token from localStorage (used for authenticated API calls)
  Initialize patientName to null (used for filtering by name)


  Add an 'input' event listener to the search bar
  On each keystroke:
    - Trim and check the input value
    - If not empty, use it as the patientName for filtering
    - Else, reset patientName to "null" (as expected by backend)
    - Reload the appointments list with the updated filter


  Add a click listener to the "Today" button
  When clicked:
    - Set selectedDate to today's date
    - Update the date picker UI to match
    - Reload the appointments for today


  Add a change event listener to the date picker
  When the date changes:
    - Update selectedDate with the new value
    - Reload the appointments for that specific date


  Function: loadAppointments
  Purpose: Fetch and display appointments based on selected date and optional patient name

  Step 1: Call getAllAppointments with selectedDate, patientName, and token
  Step 2: Clear the table body content before rendering new rows

  Step 3: If no appointments are returned:
    - Display a message row: "No Appointments found for today."

  Step 4: If appointments exist:
    - Loop through each appointment and construct a 'patient' object with id, name, phone, and email
    - Call createPatientRow to generate a table row for the appointment
    - Append each row to the table body

  Step 5: Catch and handle any errors during fetch:
    - Show a message row: "Error loading appointments. Try again later."


  When the page is fully loaded (DOMContentLoaded):
    - Call renderContent() (assumes it sets up the UI layout)
    - Call loadAppointments() to display today's appointments by default
*/

import { getAllAppointments } from './services/appointmentRecordService.js';
import { createPatientRow } from './components/patientRows.js';

const tableBody = document.getElementById('patientTableBody');
let selectedDate = new Date().toISOString().split('T')[0]; // YYYY-MM-DD
const token = localStorage.getItem('token');
let patientName = null;

// Helpers to update UI rows
function showMessageRow(message) {
  if (!tableBody) return;
  tableBody.innerHTML = `<tr><td colspan="5" style="text-align:center;">${message}</td></tr>`;
}

// Load appointments for selectedDate and optional patientName filter
export async function loadAppointments() {
  if (!tableBody) return;
  try {
    const nameParam = patientName && patientName.length ? encodeURIComponent(patientName) : 'null';
    const res = await getAllAppointments(selectedDate, nameParam, token);

  // backend may return an array of appointments or an object like { appointments: [...] } or { data: { appointments: [...] } }
  let appointments = [];
  if (Array.isArray(res)) appointments = res;
  else appointments = res?.appointments || res?.data?.appointments || [];

    tableBody.innerHTML = '';

    if (!appointments || appointments.length === 0) {
      showMessageRow('No Appointments found for the selected date.');
      return;
    }

    // For each appointment, create patient row
    appointments.forEach(app => {
      const patient = {
        id: app.patientId || app.patient?.id || 'N/A',
        name: app.patientName || app.patient?.name || 'Unknown',
        phone: app.patientPhone || app.patient?.phone || '-',
        email: app.patientEmail || app.patient?.email || '-'
      };

      const appointmentId = app.id || app.appointmentId || null;
      const doctorId = app.doctorId || app.doctor?.id || null;

      const row = createPatientRow(patient, appointmentId, doctorId);
      tableBody.appendChild(row);
    });
  } catch (error) {
    console.error('Error loading appointments:', error);
    showMessageRow('Error loading appointments. Try again later.');
  }
}

// Search and filter wiring
const searchBarEl = document.getElementById('searchBar');
if (searchBarEl) {
  searchBarEl.addEventListener('input', (e) => {
    const v = e.target.value.trim();
    patientName = v.length ? v : null;
    loadAppointments();
  });
}

const todayBtn = document.getElementById('todayButton');
if (todayBtn) {
  todayBtn.addEventListener('click', () => {
    selectedDate = new Date().toISOString().split('T')[0];
    const datePicker = document.getElementById('datePicker');
    if (datePicker) datePicker.value = selectedDate;
    loadAppointments();
  });
}

const datePickerEl = document.getElementById('datePicker');
if (datePickerEl) {
  datePickerEl.value = selectedDate;
  datePickerEl.addEventListener('change', (e) => {
    selectedDate = e.target.value || selectedDate;
    loadAppointments();
  });
}

// On page load render content and appointments
document.addEventListener('DOMContentLoaded', () => {
  if (typeof renderContent === 'function') renderContent();
  loadAppointments();
});

