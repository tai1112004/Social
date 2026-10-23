package com.taihoang.social_backend;

import com.taihoang.social_backend.Entity.LoginApprovalRequest;
import com.taihoang.social_backend.Entity.User;
import com.taihoang.social_backend.Repository.LoginApprovalRequestRepository;
import com.taihoang.social_backend.Repository.UserRepository;
import com.taihoang.social_backend.Service.EmailService;
import com.taihoang.social_backend.Service.Implements.LoginApprovalServiceImpl;
import com.taihoang.social_backend.Service.LoginApprovalService;
import com.taihoang.social_backend.Service.NotificationService;
import com.taihoang.social_backend.Service.UserSessionService;
import com.taihoang.social_backend.dto.LoginOtpVerifyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginApprovalOtpTest {

    private static final String TOKEN = "approval-token";

    private LoginApprovalRequestRepository repository;
    private RecordingTransactionManager transactionManager;
    private LoginApprovalService service;
    private LoginApprovalRequest request;

    @BeforeEach
    void setUp() {
        repository = mock(LoginApprovalRequestRepository.class);
        LoginApprovalServiceImpl target = new LoginApprovalServiceImpl(
                repository,
                mock(UserRepository.class),
                mock(UserSessionService.class),
                mock(NotificationService.class),
                mock(SimpMessagingTemplate.class),
                mock(EmailService.class)
        );
        // Run through the real @Transactional interceptor so commit vs rollback is observable.
        transactionManager = new RecordingTransactionManager();
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.addInterface(LoginApprovalService.class);
        proxyFactory.addAdvice(new TransactionInterceptor(transactionManager, new AnnotationTransactionAttributeSource()));
        service = (LoginApprovalService) proxyFactory.getProxy();

        User user = new User();
        user.setId(1L);
        user.setEmail("tai@example.test");
        request = new LoginApprovalRequest();
        request.setUser(user);
        request.setApprovalToken(TOKEN);
        request.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        request.setStatus(LoginApprovalRequest.Status.PENDING);
        request.setOtpHash("hash-of-the-real-otp");
        request.setOtpSentAt(LocalDateTime.now());
        when(repository.findByApprovalTokenForUpdate(TOKEN)).thenReturn(Optional.of(request));
    }

    @Test
    void wrongOtpAttemptIsCommittedNotRolledBack() {
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> service.verifyOtp(TOKEN, new LoginOtpVerifyRequest("000000", false))
        );

        assertEquals("OTP khong dung", exception.getMessage());
        assertEquals(1, request.getOtpAttempts());
        assertEquals(1, transactionManager.commits);
        assertEquals(0, transactionManager.rollbacks);
    }

    @Test
    void fifthWrongAttemptRejectsTheLoginRequest() {
        request.setOtpAttempts(4);

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> service.verifyOtp(TOKEN, new LoginOtpVerifyRequest("000000", false))
        );

        assertEquals("OTP da vuot qua so lan thu cho phep", exception.getMessage());
        assertEquals(LoginApprovalRequest.Status.REJECTED, request.getStatus());
        assertEquals(0, transactionManager.rollbacks);
    }

    @Test
    void resendingOtpKeepsTheAttemptCount() {
        request.setOtpAttempts(3);

        service.sendOtp(TOKEN);

        assertEquals(3, request.getOtpAttempts());
        assertNotEquals("hash-of-the-real-otp", request.getOtpHash());
    }

    @Test
    void resendingAfterExhaustedAttemptsIsRefused() {
        request.setOtpAttempts(5);

        assertThrows(BadCredentialsException.class, () -> service.sendOtp(TOKEN));

        assertEquals(LoginApprovalRequest.Status.REJECTED, request.getStatus());
        assertEquals("hash-of-the-real-otp", request.getOtpHash());
        assertEquals(0, transactionManager.rollbacks);
    }

    private static final class RecordingTransactionManager extends AbstractPlatformTransactionManager {
        int commits;
        int rollbacks;

        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            commits++;
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            rollbacks++;
        }
    }
}
