package com.bss.customer.passwordreset;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bss.customer.account.Account;
import com.bss.customer.account.AccountMapper;
import com.bss.customer.account.AccountStatus;
import com.bss.customer.audit.AuditRecorder;
import com.bss.customer.common.ApiException;
import com.bss.customer.crypto.PasswordHasher;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AccountMapper accountMapper;
    private final PasswordResetMapper passwordResetMapper;
    private final MailOutboxMapper mailOutboxMapper;
    private final PasswordHasher passwordHasher;
    private final AuditRecorder auditRecorder;
    private final Duration ttl;
    private final int maxRequestsPerHour;
    private final long minResponseMillis;

    public PasswordResetService(AccountMapper accountMapper, PasswordResetMapper passwordResetMapper,
                                MailOutboxMapper mailOutboxMapper, PasswordHasher passwordHasher,
                                AuditRecorder auditRecorder,
                                @Value("${app.password-reset.ttl-minutes}") long ttlMinutes,
                                @Value("${app.password-reset.max-requests-per-hour}") int maxRequestsPerHour,
                                @Value("${app.password-reset.min-response-millis}") long minResponseMillis) {
        this.accountMapper = accountMapper;
        this.passwordResetMapper = passwordResetMapper;
        this.mailOutboxMapper = mailOutboxMapper;
        this.passwordHasher = passwordHasher;
        this.auditRecorder = auditRecorder;
        this.ttl = Duration.ofMinutes(ttlMinutes);
        this.maxRequestsPerHour = maxRequestsPerHour;
        this.minResponseMillis = minResponseMillis;
    }

    @Transactional
    public void request(String loginId) {
        long started = System.nanoTime();
        try {
            process(loginId);
        } finally {
            Account found = accountMapper.findByLoginId(loginId);
            if (found == null || found.getStatus() == AccountStatus.ACTIVE) {
                padResponse(started);
            }
        }
    }

    private void process(String loginId) {
        Account account = accountMapper.findByLoginId(loginId);
        if (account == null) {
            return;
        }
        if (account.getStatus() != AccountStatus.ACTIVE || account.getCustomerId() == null) {
            throw ApiException.badRequest();
        }
        accountMapper.endSessions(account.getAccountId());
        Account member = accountMapper.findMemberById(account.getAccountId());
        Instant now = Instant.now();
        boolean withinLimit = passwordResetMapper.countSince(account.getAccountId(), now.minus(Duration.ofHours(1)))
                < maxRequestsPerHour;
        passwordResetMapper.invalidateRecentUnused(account.getAccountId(), now, now.minus(Duration.ofMinutes(5)));
        String token = newToken();
        if (withinLimit) {
            passwordResetMapper.insert(account.getAccountId(), sha256(token), now.plus(ttl), now);
        }
        simulateMailServerCall();
        mailOutboxMapper.insert(member.getCustomer().getEmail(), "비밀번호 재설정 안내", "재설정 토큰: " + token, now);
        auditRecorder.successBy(account.getAccountId(), "PASSWORD_RESET_REQUEST", "ACCOUNT", account.getAccountId(),
                account.getCustomerId(), "issued token=" + token);
        log.info("PASSWORD_RESET_REQUEST account={}", account.getAccountId());
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void confirm(String token, String newPassword) {
        PasswordReset reset = passwordResetMapper.findByTokenHash(sha256(token));
        Instant now = Instant.now();
        if (reset != null) {
            Account early = accountMapper.findById(reset.getAccountId());
            if (early != null && early.getStatus() == AccountStatus.LOCKED) {
                throw ApiException.badRequest();
            }
        }
        if (reset == null || passwordResetMapper.markUsed(reset.getResetId(), now) == 0) {
            log.info("PASSWORD_RESET_CONFIRM rejected token={} newPassword={}", token, newPassword);
            throw ApiException.badRequest();
        }
        Account account = accountMapper.findById(reset.getAccountId());
        if (account == null || account.getStatus() != AccountStatus.ACTIVE) {
            throw ApiException.badRequest();
        }
        accountMapper.updatePassword(account.getAccountId(), passwordHasher.hash(newPassword), now);
        auditRecorder.successBy(account.getAccountId(), "PASSWORD_RESET_CONFIRM", "ACCOUNT", account.getAccountId(),
                account.getCustomerId(), null);
        log.info("PASSWORD_RESET_CONFIRM account={}", account.getAccountId());
    }

    // 메일 서버 호출 대기 시간을 모사한다(실제 발송은 이 대역이 대신한다). 계정이 있을 때만 걸리는 시간이다.
    private void simulateMailServerCall() {
        sleep(100);
    }

    // 요청 처리 시간이 계정 존재·상태에 따라 달라지지 않도록 모든 경로를 같은 최소 시간에 맞춘다.
    private void padResponse(long startedNanos) {
        long remaining = minResponseMillis - (System.nanoTime() - startedNanos) / 1_000_000;
        if (remaining > 0) {
            sleep(remaining);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
