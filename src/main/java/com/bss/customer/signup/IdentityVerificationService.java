package com.bss.customer.signup;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.bss.customer.account.Account;
import com.bss.customer.account.AccountMapper;
import com.bss.customer.account.AccountStatus;
import com.bss.customer.common.ApiException;
import com.bss.customer.crypto.FieldEncryptor;
import com.bss.customer.customer.Customer;
import com.bss.customer.customer.CustomerMapper;

@Service
public class IdentityVerificationService {

    private final AccountMapper accountMapper;
    private final CustomerMapper customerMapper;
    private final IdentityVerificationMapper verificationMapper;
    private final AgencyClient agencyClient;
    private final FieldEncryptor fieldEncryptor;
    private final TransactionTemplate transactionTemplate;
    private final Duration ttl;

    public IdentityVerificationService(AccountMapper accountMapper,
                                       CustomerMapper customerMapper,
                                       IdentityVerificationMapper verificationMapper,
                                       AgencyClient agencyClient,
                                       FieldEncryptor fieldEncryptor,
                                       PlatformTransactionManager transactionManager,
                                       @Value("${app.identity.verification-ttl-minutes}") long ttlMinutes) {
        this.accountMapper = accountMapper;
        this.customerMapper = customerMapper;
        this.verificationMapper = verificationMapper;
        this.agencyClient = agencyClient;
        this.fieldEncryptor = fieldEncryptor;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    public VerificationResponse request(String signupRef) {
        Account account = accountMapper.findBySignupRef(signupRef);
        if (account == null) {
            throw ApiException.badRequest();
        }
        if (account.getStatus() != AccountStatus.PENDING_VERIFICATION) {
            throw verificationRejected();
        }
        String verificationId = UUID.randomUUID().toString();
        String redirectUrl = agencyClient.createRequest(verificationId);

        Instant now = Instant.now();
        IdentityVerification verification = new IdentityVerification();
        verification.setVerificationId(verificationId);
        verification.setAccountId(account.getAccountId());
        verification.setExpiresAt(now.plus(ttl));
        verification.setCreatedAt(now);
        verificationMapper.insert(verification);
        return new VerificationResponse(verificationId, redirectUrl, verification.getExpiresAt());
    }

    public VerificationCompleteResponse complete(String verificationId, String agencyResultRef) {
        IdentityVerification verification = verificationMapper.findById(verificationId);
        if (verification == null || !"REQUESTED".equals(verification.getStatus())) {
            throw verificationRejected();
        }
        Instant now = Instant.now();
        Account member = accountMapper.findMemberById(verification.getAccountId());
        if (member.getStatus() != AccountStatus.PENDING_VERIFICATION) {
            throw verificationRejected();
        }

        if (agencyResultRef == null || agencyResultRef.length() < 20 || !agencyResultRef.matches("[A-Za-z0-9_-]+")) {
            throw verificationRejected();
        }
        AgencyResult result = agencyClient.fetchResult(agencyResultRef);
        transactionTemplate.executeWithoutResult(tx -> activate(verification, null, member.getCustomerId(),
                result == null ? null : result.ci(), now));
        return new VerificationCompleteResponse(AccountStatus.ACTIVE);
    }

    private boolean isAcceptable(AgencyResult result, String verificationId, Customer customer) {
        if (result == null || !"SUCCESS".equals(result.outcome())) {
            return false;
        }
        return verificationId.equals(result.verificationId())
                && customer.getName().equals(result.name())
                && customer.getBirthDate().toString().equals(result.birthDate())
                && result.ci() != null && !result.ci().isBlank();
    }

    private void activate(IdentityVerification verification, String agencyResultRef,
                          Long customerId, String ci, Instant now) {
        try {
            if (ci != null) {
                customerMapper.updateCi(customerId, java.util.Base64.getEncoder().encodeToString(ci.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            }
            int completed = verificationMapper.markCompleted(verification.getVerificationId(), agencyResultRef, now);
            int activated = accountMapper.activateVerified(verification.getAccountId(), now);
            if (completed != 1 || activated != 1) {
                throw verificationRejected();
            }
        } catch (DuplicateKeyException e) {
            throw verificationRejected();
        }
    }

    private ApiException verificationRejected() {
        return ApiException.conflict("VERIFICATION_REJECTED", "본인 확인을 완료할 수 없습니다.");
    }
}
