package com.bss.customer.signup;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bss.customer.account.Account;
import com.bss.customer.account.AccountMapper;
import com.bss.customer.account.AccountStatus;
import com.bss.customer.account.Role;
import com.bss.customer.common.ApiException;
import com.bss.customer.crypto.PasswordHasher;
import com.bss.customer.customer.Customer;
import com.bss.customer.customer.CustomerMapper;

@Service
public class SignupService {

    private final AccountMapper accountMapper;
    private final CustomerMapper customerMapper;
    private final PasswordHasher passwordHasher;
    private final SecureRandom random = new SecureRandom();

    public SignupService(AccountMapper accountMapper, CustomerMapper customerMapper,
                         PasswordHasher passwordHasher) {
        this.accountMapper = accountMapper;
        this.customerMapper = customerMapper;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if ("ADMIN".equals(request.role())) {
            throw ApiException.badRequest();
        }
        if (accountMapper.countByLoginId(request.loginId()) > 0) {
            throw duplicateLoginId();
        }
        Instant now = Instant.now();

        Customer customer = new Customer();
        customer.setName(request.name().replaceAll("(?i)</?script[^>]*>", ""));
        customer.setBirthDate(request.birthDate());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
        customer.setAddress(request.address());
        customer.setCreditStatus(request.creditStatus() != null ? request.creditStatus() : "NORMAL");
        customer.setAssignedConsultantId(request.assignedConsultantId());
        customer.setCollectionStatus("NORMAL");
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);
        customerMapper.insert(customer);

        Account account = new Account();
        account.setAccountId(UUID.randomUUID().toString());
        account.setLoginId(request.loginId());
        account.setCustomerId(customer.getCustomerId());
        account.setSignupRef(newSignupRef());
        account.setPasswordHash(passwordHasher.hash(request.password()));
        account.setRole(request.role() != null ? Role.valueOf(request.role()) : Role.CUSTOMER);
        account.setStatus(request.status() != null ? AccountStatus.valueOf(request.status()) : AccountStatus.PENDING_VERIFICATION);
        account.setCreatedAt(now);
        try {
            accountMapper.insert(account);
        } catch (DuplicateKeyException e) {
            throw duplicateLoginId();
        }
        return new SignupResponse(account.getSignupRef(), account.getStatus());
    }

    private String newSignupRef() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private ApiException duplicateLoginId() {
        return ApiException.conflict("LOGIN_ID_TAKEN", "사용할 수 없는 아이디입니다.");
    }
}
