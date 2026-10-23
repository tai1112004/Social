package com.taihoang.social_backend.exception;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(Long postId) {
        super("Khong tim thay bai viet: " + postId);
    }
}
