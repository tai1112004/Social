package com.taihoang.social_backend.Repository;

import com.taihoang.social_backend.Entity.CallHistory;
import com.taihoang.social_backend.Entity.CallStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface CallHistoryRepository extends JpaRepository<CallHistory, Long> {
    Optional<CallHistory> findByCallId(String callId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CallHistory c where c.callId = :callId")
    Optional<CallHistory> findByCallIdForUpdate(@Param("callId") String callId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<CallHistory> findByStatusAndCreatedAtBefore(CallStatus status, LocalDateTime cutoff);

    @Query("""
            select c
            from CallHistory c
            join fetch c.conversation
            join fetch c.caller
            join fetch c.callee
            where c.caller.id = :userId or c.callee.id = :userId
            order by c.createdAt desc, c.id desc
            """)
    List<CallHistory> findForUser(@Param("userId") Long userId, Pageable pageable);

    boolean existsByCallIdAndStatusIn(String callId, List<CallStatus> statuses);

    @Query("""
            select case when count(c) > 0 then true else false end
            from CallHistory c
            where (c.caller.id = :userId or c.callee.id = :userId)
              and c.status in :statuses
            """)
    boolean existsForUserWithStatusIn(@Param("userId") Long userId, @Param("statuses") List<CallStatus> statuses);

    // Rows from before last_activity_at existed fall back to answeredAt/createdAt.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c
            from CallHistory c
            where c.status = :status
              and coalesce(c.lastActivityAt, c.answeredAt, c.createdAt) < :cutoff
            """)
    List<CallHistory> findInactiveForUpdate(@Param("status") CallStatus status, @Param("cutoff") LocalDateTime cutoff);
}
