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
  	date TEXT NOT NULL,
  	serviceTime TEXT NOT NULL,
  	dentist TEXT NOT NULL,
  	dentalService TEXT NOT NULL
);
```