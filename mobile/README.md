# Social Mobile

Native Android client for the existing social network backend.

## Open in Android Studio

1. Open the `mobile/` folder as a project.
2. Let Android Studio sync the Gradle project and install missing SDK/dependencies.
3. Create or select an emulator with API 34+.
4. Run the `app` configuration.

The app now uses the backend for login, registration, feed, profile, notifications, notification read state, session persistence, and logout. The remaining REST methods are centralized in `ApiClient` for the next screens; realtime STOMP chat still requires a separate implementation.

## Current screens

- Login
- Facebook-inspired home feed and stories
- Friends suggestions
- Messenger-inspired conversation list
- Notifications
- Profile and security entry point

## Backend integration target

For a physical phone connected by USB, start the backend first, then run `powershell -ExecutionPolicy Bypass -File .\usb-dev.ps1 -DeviceId <adb-id>` from this folder. The script enables `adb reverse tcp:8080 tcp:8080`, builds the app with `http://127.0.0.1:8080`, and installs it on the selected phone. This avoids depending on a changing Wi-Fi IP. The backend itself must be running on the computer at port `8080`.

For an emulator or a phone on the same LAN, override the base URL with `-PapiBaseUrl=http://10.0.2.2:8080` for an emulator, or `-PapiBaseUrl=http://<computer-ip>:8080` for a physical phone. Cleartext HTTP is already allowed for local development. `localhost` from an emulator refers to the emulator itself.

The backend exposes 103 REST routes. `ApiClient` currently covers authentication/session, feed, reactions, profile, notifications, conversations/messages, friends, stories, and user search. Admin routes are intentionally excluded from the normal user mobile shell. WebSocket/STOMP chat destinations still need to be connected to the conversation UI.
