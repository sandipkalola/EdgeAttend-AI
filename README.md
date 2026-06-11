# EdgeAttend AI

AI-Powered Offline Biometric Attendance System

[![Get it on Google Play](https://img.shields.io/badge/Google%20Play-Download-success?style=for-the-badge&logo=google-play)](https://play.google.com/store/apps/details?id=com.sk.edgeattend)
## Overview

EdgeAttend AI is a secure, offline-first attendance management solution that leverages facial recognition, anti-spoof liveness detection, GPS verification, and encrypted local storage to accurately record employee attendance.

Designed for businesses, factories, construction sites, warehouses, schools, and remote work locations, EdgeAttend AI operates entirely on-device and does not require constant internet connectivity.

---

## Key Features

### AI Face Recognition
- Fast employee identification using facial biometrics
- On-device face matching
- Secure biometric template storage

### Anti-Spoof Liveness Detection
Multiple verification layers including:
- Smile detection
- Eye blink validation
- Head pose verification
- Real-time selfie validation

### Employee Enrollment
- Register employees with unique IDs
- Multi-angle face capture
- Secure biometric profile generation
- Local database storage

### Attendance Tracking
- Check-In support
- Check-Out support
- Real-time verification
- Instant attendance confirmation

### GPS Location Verification
- Captures real device GPS coordinates
- Validates employee presence on-site
- Stores attendance location history

### Offline First Architecture
- Works without internet connection
- Local attendance processing
- Edge AI inference
- High-speed verification

### Secure Local Storage
- AES-256 encrypted database
- Secure biometric template protection
- PIN-protected administration access

### Staff Database Management
- Search employees by ID or name
- View registered staff profiles
- Delete employee records

### Attendance Logs
- Attendance history tracking
- GPS location records
- Match confidence scores
- Check-In and Check-Out logs

### Backup & Synchronization
- Local database export
- JSON backup generation
- Server synchronization support

---

## Security Features

- AES-256 Encrypted Storage
- Secure PIN Authentication
- Offline Data Processing
- Protected Biometric Templates
- Local Database Isolation
- Emergency Data Wipe Functionality

---

## Workflow

### Employee Registration

1. Open Enroll Employee
2. Enter Employee ID and Name
3. Capture biometric face data
4. Verify facial signatures
5. Save profile to local database

### Attendance Verification

1. Open Verify Face Scan
2. Select Check-In or Check-Out
3. Face detection starts automatically
4. AI verifies identity and liveness
5. GPS location is captured
6. Attendance log is created

---

## Modules

### Dashboard
- Registered employee count
- Daily attendance count
- System status monitoring

### Verify Face Scan
- Real-time face verification
- Liveness detection
- Attendance logging

### Employee Enrollment
- Staff registration
- Biometric profile creation

### Staff Database
- Employee management
- Search and delete operations

### Historical Logs
- Attendance records
- GPS tracking history

### Settings Wizard
- Security PIN management
- GPS diagnostics
- Backup & sync
- Database maintenance

---

## Technology Highlights

- Edge AI Processing
- Facial Recognition
- Liveness Detection
- GPS Verification
- AES-256 Encryption
- Local SQLite Storage
- Offline-First Architecture

---

## Use Cases

- Corporate Offices
- Manufacturing Plants
- Warehouses
- Construction Sites
- Educational Institutions
- Retail Stores
- Healthcare Facilities
- Remote Field Operations

---

## Benefits

- No biometric hardware required
- Works offline
- Faster attendance processing
- Reduced buddy punching
- GPS-backed verification
- Enhanced employee accountability
- Secure local data ownership

---

## Future Enhancements

- Cloud Synchronization
- Admin Web Dashboard
- Multi-Device Sync
- Attendance Analytics
- Shift Management
- Leave Management
- Payroll Integration
- QR & NFC Support

## CI/CD Setup

This project uses GitHub Actions for continuous integration and automated releases.

### 1. Configure GitHub Secrets
To enable automated builds and releases, go to your repository **Settings > Secrets and variables > Actions** and add the following secrets:

#### For Continuous Integration (CI):
- `DEBUG_KEYSTORE_BASE64`: The content of your `debug.keystore.base64` file.

#### For Automated Releases:
- `RELEASE_KEYSTORE_BASE64`: The base64-encoded content of your `EdgeAttend.jks` file.
- `RELEASE_STORE_PASSWORD`: Keystore password.
- `RELEASE_KEY_ALIAS`: Key alias.
- `RELEASE_KEY_PASSWORD`: Key password.

> **Tip:** You can generate the base64 string for your keystore using:
> `certutil -encode EdgeAttend.jks tmp.txt && type tmp.txt` (Windows) or `base64 EdgeAttend.jks` (Linux/Mac).

### 2. Workflows
- **Android CI:** Runs on every push to `main`. It builds the debug APK, runs tests, and checks linting.
- **Android Release:** Triggers when a tag starting with `v` is pushed (e.g., `git tag v1.2.0 && git push origin v1.2.0`). It builds the signed **AAB** and creates a GitHub release.

---

## 📸 Screenshots


<img src="https://github.com/sandipkalola/EdgeAttend-AI/blob/main/assets/img_1.jpeg?raw=true" height = "500px"> <img src="https://github.com/sandipkalola/EdgeAttend-AI/blob/main/assets/img_2.jpeg?raw=true" height = "500px"> <img src="https://github.com/sandipkalola/EdgeAttend-AI/blob/main/assets/img_3.jpeg?raw=true" height = "500px"> <img src="https://github.com/sandipkalola/EdgeAttend-AI/blob/main/assets/img_4.jpeg?raw=true" height = "500px"> <img src="https://github.com/sandipkalola/EdgeAttend-AI/blob/main/assets/img_5.jpeg?raw=true" height = "500px"> <img src="https://github.com/sandipkalola/EdgeAttend-AI/blob/main/assets/img_6.jpeg?raw=true" height = "500px"> <img src="https://github.com/sandipkalola/EdgeAttend-AI/blob/main/assets/img_7.jpeg?raw=true" height = "500px"> <img src="https://github.com/sandipkalola/EdgeAttend-AI/blob/main/assets/img_8.jpeg?raw=true" height = "500px"> <img src="https://github.com/sandipkalola/EdgeAttend-AI/blob/main/assets/img_9.jpeg?raw=true" height = "500px"> <img src="https://github.com/sandipkalola/EdgeAttend-AI/blob/main/assets/img_10.jpeg?raw=true" height = "500px"> <img src="https://github.com/sandipkalola/EdgeAttend-AI/blob/main/assets/img_11.jpeg?raw=true" height = "500px">

# EdgeAttend AI

![License](https://img.shields.io/badge/License-MIT-green.svg)

AI-Powered Offline Biometric Attendance System.
