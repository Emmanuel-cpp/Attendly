# Attendly — Smart Attendance Tracker

A native Android app that replaces paper attendance registers with secure, rotating QR codes.

Final-year capstone project · CS 455 Mobile Programming · The Copperbelt University

---

## The problem

Paper attendance registers are slow, easy to forge, and give lecturers no usable data. A sheet passed around a lecture hall wastes minutes of class time, and a student can trivially sign in for an absent friend.

## How Attendly works

1. A lecturer starts a session for a course. The app displays a QR code on screen.
2. The QR **rotates every 20 seconds** — a screenshot shared after class is already expired.
3. Students scan it with the app. They are marked present instantly, tied to their authenticated identity.
4. The lecturer watches the roster fill in live, then ends the session.
5. Students can view their own attendance history per course.

## Anti-spoofing design

The core security problem in QR attendance is that a QR code can be photographed and shared. Attendly addresses this with two layers:

**Rotating time-bound tokens (TOTP-style).** Each session generates a cryptographically random secret. Time is divided into 20-second slots, and the token embedded in the QR is `HMAC-SHA256(secret, slot)`. The QR therefore changes every 20 seconds. A shared screenshot is only valid for seconds, not for the whole class.

**One scan per authenticated student.** Attendance records are stored at `sessions/{sessionId}/attendance/{studentUid}` — the student's UID *is* the document ID, so duplicate check-ins are structurally impossible, not merely validated against.

Token verification accepts the current and previous slot to tolerate clock skew and network latency, following standard TOTP practice.

## Architecture

- **Kotlin + Jetpack Compose** — declarative UI, no XML layouts
- **MVVM** — Screen → ViewModel → Repository → Firebase, with unidirectional data flow via `StateFlow`
- **Firebase Auth** — identity; role (student / lecturer / admin) resolved from a Firestore `users/{uid}` document
- **Cloud Firestore** — courses, sessions, attendance records
- **CameraX + ML Kit** — QR scanning
- **ZXing** — QR generation
- **Offline-first** — persistent Firestore cache with cache-first reads and queued writes, so the app stays usable on slow or intermittent networks

Real-time updates use Firestore snapshot listeners: when a student scans, the lecturer's roster updates within a second, with no refresh.

## Features implemented

- Email/password authentication with role-based routing
- Lecturer: create courses, start/end sessions, live rotating QR, live attendance roster
- Persistent "session in progress" banner across the app (mini-player pattern)
- Student: camera QR scanning with permission handling, success/expiry/duplicate feedback
- Student: attendance history via Firestore collection group query
- Offline-first data layer

## Roadmap

- Server-side token verification (Cloud Function) so the session secret is never readable by clients
- Production Firestore security rules replacing development test-mode rules
- Lecturer reports and export (PDF / Excel)
- Student self-registration
- Institutional provisioning: CSV class-list import, then student-records-system integration
- Attendance vs. performance analytics (early-warning signal for at-risk students)

## Known limitations

Token verification is currently performed client-side, which means the session secret is readable by an authenticated client. This is a deliberate scope decision for the prototype; the production path is a Cloud Function that verifies tokens server-side. Firestore is also currently in test mode; production rules are a priority item.

## Running the project

1. Clone the repo and open in Android Studio (Empty Activity / Compose, min SDK 24+).
2. Create a Firebase project and register an Android app with the package name `com.siamoonga.attendance`.
3. Enable **Email/Password** authentication and create a **Cloud Firestore** database.
4. Download your own `google-services.json` and place it in `app/`.
5. Create a user in Firebase Auth, then add a matching document in Firestore at `users/{uid}` with fields `name` (string) and `role` (`student` | `lecturer` | `admin`).
6. Build and run.

## Author

Siamoonga — The Copperbelt University
