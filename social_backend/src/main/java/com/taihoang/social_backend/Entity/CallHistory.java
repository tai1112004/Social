package com.taihoang.social_backend.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "call_history", indexes = {
        @Index(name = "idx_call_history_caller_created", columnList = "caller_id, created_at"),
        @Index(name = "idx_call_history_callee_created", columnList = "callee_id, created_at")
})
@Getter
@Setter
public class CallHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_id", nullable = false, unique = true, length = 36)
    private String callId = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversations conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caller_id", nullable = false)
    private User caller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "callee_id", nullable = false)
    private User callee;

    @Enumerated(EnumType.STRING)
    @Column(name = "call_type", nullable = false, length = 16)
    private CallType callType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private CallStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "ended_by")
    private Long endedBy;

    @Column(name = "failure_reason", length = 200)
    private String failureReason;

    // Refreshed by every signal and by the clients' call.heartbeat; an ACCEPTED call that stops
    // receiving it is ended by CallService.expireStaleCalls.
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = CallStatus.RINGING;
        if (lastActivityAt == null) lastActivityAt = createdAt;
    }
}
