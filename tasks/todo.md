# Android mobile implementation

- [x] Create Android Compose project foundation.
- [x] Build the first visual slice with login, feed, chat, notifications, and profile.
- [x] Verify Android Studio import/build instructions.
- [x] Connect authentication and first REST vertical slice (feed, profile, notifications, reactions client).
- [x] Add real mobile registration flow using `/auth/register`.
- [x] Connect WebSocket chat and reliability flows.

## Chat reliability follow-up

- [x] Reconnect refreshes the open conversation without losing older history.
- [x] Preserve monotonic delivered/seen state during message merges.
- [x] Mark stalled sends failed after a bounded ACK timeout and allow retry.
- [x] Clear stale typing state and refresh presence after reconnect/resume.
- [x] Deduplicate paginated history and offline sync consistently.
- [ ] Complete remaining REST-backed screens (stories, friends, conversations, security, reports).

## Completed slice: Upload/media

- [x] Align post/media DTOs with `PostMediaResponse`.
- [x] Fix media-only post creation and attachment-only chat sending.
- [x] Add multi-file post selection and chat attachment preview/removal.
- [x] Verify upload errors, size limits, and Android build.

## Follow-up risks

- [ ] Stream large uploads instead of buffering the whole file in a `ByteArray`.
- [ ] Add byte-level upload progress/cancel support.
- [ ] Remove abandoned temporary uploads when a composer is cancelled.
