# Backend API catalog

Base URL: `http://localhost:8080` (Android emulator: `http://10.0.2.2:8080`). Authenticated routes require `Authorization: Bearer <JWT>`.

## Authentication

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /auth/login-approvals/{approvalToken}/status`
- `POST /auth/login-approvals/{approvalToken}/otp/send`
- `POST /auth/login-approvals/{approvalToken}/otp/verify`

## Feed, posts and reactions

- `GET /user/feed`
- `GET /user/feed/videos`
- `POST /user/feed/{postId}/reactions`
- `DELETE /user/feed/{postId}/reactions`
- `GET /user/feed/{postId}/reactions`
- `POST /user/posts`
- `GET /user/posts/{postId}`
- `PUT /user/posts/{postId}`
- `DELETE /user/posts/{postId}`
- `GET /user/posts/saved`
- `GET /user/posts/user/{userId}`
- `POST /user/posts/{postId}/save`
- `DELETE /user/posts/{postId}/save`
- `POST /user/posts/{postId}/share`
- `POST /user/posts/{postId}/hide`
- `DELETE /user/posts/{postId}/hide`

## Comments

- `GET /user/posts/{postId}/comments`
- `POST /user/posts/{postId}/comments`
- `PUT /user/posts/comments/{commentId}`
- `DELETE /user/posts/comments/{commentId}`
- `POST /user/posts/comments/{commentId}/replies`

## Profile and people

- `GET /user/profile/me`
- `PUT /user/profile/me`
- `GET /user/profile/{userId}`
- `GET /user/profile/search`
- `GET /user/friends`
- `GET /user/friends/{userId}/friends`
- `GET /user/friends/status/{userId}`
- `GET /user/friends/requests/received`
- `GET /user/friends/requests/sent`
- `POST /user/friends/requests/{receiverId}`
- `POST /user/friends/requests/{requestId}/accept`
- `POST /user/friends/requests/{requestId}/reject`
- `DELETE /user/friends/requests/{requestId}`
- `DELETE /user/friends/{userId}`

## Stories

- `GET /user/stories/feed`
- `GET /user/stories/me`
- `GET /user/stories/{storyId}`
- `GET /user/stories/{storyId}/viewers`
- `POST /user/stories`
- `POST /user/stories/{storyId}/view`
- `POST /user/stories/{storyId}/reaction`
- `POST /user/stories/{storyId}/reply`
- `DELETE /user/stories/{storyId}`
- `DELETE /user/stories/{storyId}/reaction`

## Conversations and messages

- `GET /user/conversations`
- `GET /user/conversations/{conversationId}`
- `GET /user/conversations/{conversationId}/messages`
- `POST /user/conversations/direct`
- `POST /user/conversations/group`
- `POST /user/conversations/{conversationId}/members`
- `POST /user/conversations/{conversationId}/seen`
- `PATCH /user/conversations/{conversationId}/name`
- `PATCH /user/conversations/{conversationId}/avatar`
- `PATCH /user/conversations/{conversationId}/members/{memberId}/role`
- `DELETE /user/conversations/{conversationId}/members/{memberId}`
- `DELETE /user/conversations/{conversationId}/members/me`
- `GET /user/chat/sync`
- `POST /user/chat/uploads`

## Notifications and presence

- `GET /user/notifications`
- `GET /user/notifications/unread-count`
- `PATCH /user/notifications/{notificationId}/read`
- `PATCH /user/notifications/read-all`
- `GET /presence/{userId}`

## Music and reports

- `GET /user/redis`
- `GET /user/music/tracks`
- `GET /user/music/tracks/search`
- `GET /user/music/tracks/{trackId}`
- `GET /user/reports`
- `POST /user/reports/posts/{postId}`
- `POST /user/reports/comments/{commentId}`
- `POST /user/reports/users/{userId}`

## Security

- `GET /user/security/sessions`
- `DELETE /user/security/sessions/{sessionId}`
- `DELETE /user/security/sessions/others`
- `PATCH /user/security/devices/{deviceId}/trust`
- `PATCH /user/security/devices/{deviceId}/untrust`
- `POST /user/security/login-approvals/{approvalId}/approve`
- `POST /user/security/login-approvals/{approvalId}/reject`

## Admin

- `GET /admin/statistics/overview`
- `GET /admin/statistics/growth`
- `GET /admin/statistics/active-users`
- `GET /admin/statistics/chat`
- `GET /admin/statistics/reports`
- `GET /admin/statistics/stories`
- `GET /admin/statistics/top-posts`
- `GET /admin/reports`
- `GET /admin/reports/{reportId}`
- `PATCH /admin/reports/{reportId}/review`
- `PATCH /admin/reports/{reportId}/resolve`
- `PATCH /admin/reports/{reportId}/reject`
- `PATCH /admin/users/{userId}/suspend`
- `PATCH /admin/users/{userId}/unsuspend`
- `DELETE /admin/posts/{postId}`
- `DELETE /admin/comments/{commentId}`

## WebSocket/STOMP

- Endpoint: `/ws`
- Client prefix: `/app`
- User prefix: `/user`
- `/app/chat.send`
- `/app/chat.delivered`
- `/app/chat.seen`
- `/app/chat.seenConversation`
- `/app/chat.edit`
- `/app/chat.recall`
- `/app/chat.deleteForMe`
- `/app/chat.react`
- `/app/chat.typing`

Admin routes are intentionally separated from the normal user mobile shell. The mobile client should expose them only in an admin role-specific area.
