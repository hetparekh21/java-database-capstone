<!-- Define a MySQL database design section:
Include at least 4 tables (e.g., patients, doctors, appointments, and admin)
For each table, specify column names, data types, primary and foreign keys, and constraints (e.g., NOT NULL, UNIQUE)
Define a MongoDB collection design section:
Choose a suitable document collection (e.g., prescriptions, feedback, and logs)
Provide a realistic JSON example of a document with nested fields or arrays.
Justify your design decisions using inline comments if needed.
 -->


# Schema Design

## MySQL Database Design
### Tables
#### Patients Table
- `patient_id` (INT, PRIMARY KEY, AUTO_INCREMENT)
- `first_name` (VARCHAR(50), NOT NULL)
- `last_name` (VARCHAR(50), NOT NULL)
- `date_of_birth` (DATE, NOT NULL)
- `email` (VARCHAR(100), UNIQUE, NOT NULL)
- `phone_number` (VARCHAR(15), UNIQUE, NOT NULL)
- `address` (VARCHAR(255), NOT NULL)
#### Doctors Table
- `doctor_id` (INT, PRIMARY KEY, AUTO_INCREMENT)
- `first_name` (VARCHAR(50), NOT NULL)
- `last_name` (VARCHAR(50), NOT NULL)
- `specialization` (VARCHAR(100), NOT NULL)
- `email` (VARCHAR(100), UNIQUE, NOT NULL)
- `phone_number` (VARCHAR(15), UNIQUE, NOT NULL)
#### Appointments Table
- `appointment_id` (INT, PRIMARY KEY, AUTO_INCREMENT)
- `patient_id` (INT, FOREIGN KEY REFERENCES Patients(patient_id), NOT NULL)
- `doctor_id` (INT, FOREIGN KEY REFERENCES Doctors(doctor_id), NOT NULL)
- `appointment_date` (DATETIME, NOT NULL)
- `status` (VARCHAR(20), NOT NULL)
#### Admin Table
- `admin_id` (INT, PRIMARY KEY, AUTO_INCREMENT)
- `username` (VARCHAR(50), UNIQUE, NOT NULL)
- `password` (VARCHAR(255), NOT NULL)
- `email` (VARCHAR(100), UNIQUE, NOT NULL)

## MongoDB Collection Design
### Prescriptions Collection
#### Document Example
```json
{
  "prescription_id": "609c1f2e8f1b2ca3d4e5f678",
  "patient_id": 1,
  "doctor_id": 2,
  "date_issued": "2024-06-15T10:30:00Z",
  "medications": [
    {
      "name": "Amoxicillin",
      "dosage": "500mg",
      "frequency": "3 times a day",
      "duration": "7 days"
    },
    {
      "name": "Ibuprofen",
      "dosage": "200mg",
      "frequency": "as needed",
      "duration": "5 days"
    }
  ],
  "notes": "Take medications with food to avoid stomach upset."
}
```
### Justification
- **MySQL Design**: The relational design with separate tables for patients, doctors, appointments, and admin allows for efficient data management and enforces data integrity through foreign key constraints. Unique constraints on email and phone numbers prevent duplicate entries.
- **MongoDB Design**: The prescriptions collection is designed to accommodate the flexible nature of prescription data, allowing for nested medication details. This structure supports varying numbers of medications per prescription without the need for complex joins, enhancing performance for read operations.

