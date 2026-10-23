package com.taihoang.social_backend.dto;

import com.taihoang.social_backend.Entity.LoginApprovalRequest;
import java.time.LocalDateTime;

public record PendingLoginApprovalResponse(
        Long id,
        Long userDeviceId,
        String deviceName,
        String deviceType,
        String browser,
        String os,
        String ipAddress,
        int riskScore,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        LoginApprovalRequest.Status status
) {}
