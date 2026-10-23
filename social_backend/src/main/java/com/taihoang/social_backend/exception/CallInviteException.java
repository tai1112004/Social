package com.taihoang.social_backend.exception;

/**
 * An invite that cannot start a call. It gets its own STOMP error code so the caller's client can
 * stop its "calling..." screen instead of waiting for the ringing timeout.
 */
public class CallInviteException extends IllegalArgumentException {
    public CallInviteException(String message) {
        super(message);
    }
}
