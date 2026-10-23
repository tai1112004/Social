package com.taihoang.social_backend.exception;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(Long notificationId) {
        super("Khong tim thay notification: " + notificationId);
    }
}
