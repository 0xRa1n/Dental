# SQL Scripts (in DBeaver)
1. Create the users table:
```
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    full_name TEXT NOT NULL,
    role TEXT DEFAULT 'patient',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

2. Create the appointments table:
```
CREATE TABLE IF NOT EXISTS appointments (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL,
	serviceDate DATE NOT NULL,
  	date DATE NOT NULL,
  	serviceTime TEXT NOT NULL,
  	dentist TEXT NOT NULL,
  	dentalService TEXT NOT NULL,
    status TEXT NOT NULL,
	notes LONGTEXT NOT NULL
);
```

3. For the doctor's time, schedule, and blocked dates
```
CREATE TABLE doctor_schedule (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dentist VARCHAR(255) NOT NULL,
    start_time VARCHAR(50) NOT NULL,
    end_time VARCHAR(50) NOT NULL
);

CREATE TABLE doctor_recurring_days (
    schedule_id INTEGER,
    day_of_week VARCHAR(20),
    FOREIGN KEY(schedule_id) REFERENCES doctor_schedule(id)
);

CREATE TABLE doctor_blocked_dates (
    dentist VARCHAR(255) NOT NULL,
    blocked_date DATE NOT NULL
);
```
3. Create an archive for deleted appointments
```
CREATE TABLE IF NOT EXISTS deleted_appointments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    appointment_id INTEGER NOT NULL,
    username TEXT NOT NULL,
	serviceDate DATE NOT NULL,
    date DATE NOT NULL,
    serviceTime TEXT NOT NULL,
    dentist TEXT NOT NULL,
    dentalService TEXT NOT NULL,
    status TEXT NOT NULL,
    notes TEXT NOT NULL,
    FOREIGN KEY(appointment_id) REFERENCES appointments(id)
);
```
