<!-- document your user stories and save them in the repository.
Define Admin User Stories: Develop user stories for admins who manage access and system functionality.
Define Patient User Stories: Develop user stories for patients who book and manage appointments.
Define Doctor User Stories: Develop user stories for doctors who manage their availability and patient appointments.
Commit your stories: Push your user stories to GitHub and ensure the changes are stored correctly.
 -->

<!-- # User Story Template

**Title:**
_As a [user role], I want [feature/goal], so that [reason]._

**Acceptance Criteria:**
1. [Criteria 1]
2. [Criteria 2]
3. [Criteria 3]

**Priority:** [High/Medium/Low]
**Story Points:** [Estimated Effort in Points]
**Notes:**
- [Additional information or edge cases] -->

<!-- As an admin, you can:

Log into the portal with your username and password to manage the platform securely
Log out of the portal to protect system access
Add doctors to the portal
Delete doctor's profile from the portal
Run a stored procedure in MySQL CLI to get the number of appointments per month and track usage statistics -->

<!-- As a patient, you can:

View a list of doctors without logging in to explore options before registering
Sign up using your email and password to book appointments
Log into the portal to manage your bookings
Log out of the portal to secure your account
Log in and book an hour-long appointment to consult with a doctor
View my upcoming appointments so that I can prepare accordingly
 -->

 <!-- As a doctor, you can:

Log into the portal to manage your appointments
Log out of the portal to protect my data
View my appointment calendar to stay organized
Mark your unavailability to inform patients only the available slots
Update your profile with specialization and contact information so that patients have up-to-date information
View the patient details for upcoming appointments so that I can be prepared -->


# User Stories

## Admin User Stories
**Title:**
_As an admin, I want to manage user roles and permissions, so that I can control access to different parts of the system._

**Acceptance Criteria:**
1. Admin can view all users and their roles.
2. Admin can assign roles to users.
3. Admin can revoke roles from users.

**Priority:** High
**Story Points:** 5
**Notes:**
- Admin should have a clear overview of user roles.

## Patient User Stories
**Title:**
_As a patient, I want to book an appointment with a doctor, so that I can receive medical care._

**Acceptance Criteria:**
1. Patient can view available doctors.
2. Patient can select a doctor and book an appointment.
3. Patient receives a confirmation of the appointment.

**Priority:** High
**Story Points:** 8
**Notes:**
- Appointment reminders should be sent to patients.

## Doctor User Stories
**Title:**
_As a doctor, I want to manage my availability, so that patients can only book appointments when I'm free._

**Acceptance Criteria:**
1. Doctor can view their calendar.
2. Doctor can block off time slots.
3. Doctor can update their availability.

**Priority:** High
**Story Points:** 8
**Notes:**
- Doctors should be notified of new appointments.
