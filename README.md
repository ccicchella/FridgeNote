# FridgeNote

A task management Android app for families, friends, and roommates to create and share household chores. Built as part of MSU's CSE 476 (Mobile Application Development) course.

## Features

- User login and account creation with Firebase Authentication
- Dashboard displaying active and completed tasks
- Add tasks with a name, point value, urgency level, and optional photo requirement
- Camera integration to attach photos as proof of task completion
- Cloud sync via Firebase Firestore so tasks persist across devices and users
- Supports both portrait and landscape orientations

## Tech Stack

Java, Android SDK, Firebase Authentication, Firebase Firestore, Android Data Binding, Gradle

## Setup

Requirements: Android Studio, Android SDK

1. Clone the repo and open in Android Studio
2. Add your own `google-services.json` to `app/` (required for Firebase)
3. Let Gradle sync, then run on an emulator or physical device

## Author

Charles Cicchella
