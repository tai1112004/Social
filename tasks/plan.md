# Implementation Plan: Android Social Mobile App

## Overview

Build a native Android client for the existing social-network backend. The first slice provides a polished, runnable Compose shell with authentication entry, a Facebook-inspired feed, Messenger-inspired chat list, and mobile navigation. Later slices will connect the existing REST and WebSocket contracts.

## Architecture Decisions

- Kotlin + Jetpack Compose + Material 3 for a native Android experience.
- Keep the mobile client in `mobile/` so the existing web and backend projects remain untouched.
- Keep the Compose shell small and introduce a shared REST client/session store before adding screen-specific repositories. Use the existing backend contracts as the source of truth.
- Reuse backend routes under `/auth`, `/user/posts`, `/user/conversations`, `/user/notifications`, and STOMP destinations under `/app/chat` and `/user/queue`.

## Task List

### Phase 1: Foundation

- [ ] Task 1: Create the Android Compose project and build configuration.
- [ ] Task 2: Create the visual shell: login, feed, chat, notifications, and profile tabs.

### Checkpoint: First runnable slice

- [ ] Android Studio can open and sync the project.
- [ ] The app builds and launches on an emulator.
- [ ] Bottom navigation switches between the main mobile surfaces.

### Phase 2: Backend integration

- [x] Task 3: Add REST client, persistent authentication flow, and error handling.
- [x] Task 4: Load feed, profile, reactions client, and notifications from the backend.
- [x] Task 5: Complete STOMP chat reliability: ordering/deduplication, monotonic delivery state, send timeout/retry, typing/presence recovery, history synchronization, and reconnect handling.

#### Task 5 remaining slices

- [x] 5a. Preserve monotonic message state while merging ACK, history, and realtime payloads.
- [x] 5b. Add a bounded Android send-ack timeout and explicit retry/failure state.
- [x] 5c. Expire stale typing state and refresh presence after reconnect/lifecycle resume.
- [x] 5d. Harden history pagination and offline synchronization deduplication.

### Phase 3: Product polish

- [ ] Task 6: Add post/story creation, friends, search, saved posts, and security settings.
- [ ] Task 7: Add loading/error/empty states, accessibility labels, dark theme, and offline-friendly state.

### Phase 4: Upload/media integration

- [x] Task 8: Align mobile upload DTOs and post validation with the backend contract.
- [x] Task 9: Support attachment-only chat messages and multi-file selection with removable previews.
- [x] Task 10: Add upload failure/size states and verify all upload entry points build successfully.

### Checkpoint: Upload/media slice

- [x] Media-only posts are accepted by the backend contract.
- [x] Chat messages containing only attachments can be sent.
- [x] Mobile build succeeds and failed uploads are visible/recoverable.

## Risks and Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Android dependency versions are not cached locally | Medium | Use a conservative AGP/Kotlin baseline and verify Gradle sync early. |
| REST/WebSocket payloads evolve independently | High | Define DTOs from the current backend controllers and integrate one vertical flow at a time. |
| Chat reconnect and pending message behavior is complex | High | Port the existing web reliability behavior after the basic STOMP connection works. |

## Open Questions

- Confirm the backend base URL for emulator and physical-device testing before REST integration.
- Confirm whether admin screens are part of the first mobile release.
