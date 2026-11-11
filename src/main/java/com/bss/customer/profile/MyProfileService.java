package com.bss.customer.profile;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bss.customer.account.Account;
import com.bss.customer.account.AccountMapper;
import com.bss.customer.common.ApiException;
import com.bss.customer.crypto.PasswordHasher;
import com.bss.customer.customer.CustomerMapper;
import com.bss.customer.masking.CustomerResponseMapper;
import com.bss.customer.security.LoginAccount;

@Service
public class MyProfileService {

    private final AccountMapper accountMapper;
    private final CustomerMapper customerMapper;
    private final CustomerResponseMapper responseMapper;
    private final PasswordHasher passwordHasher;

    public MyProfileService(AccountMapper accountMapper, CustomerMapper customerMapper,
                            CustomerResponseMapper responseMapper, PasswordHasher passwordHasher) {
        this.accountMapper = accountMapper;
        this.customerMapper = customerMapper;
        this.responseMapper = responseMapper;
        this.passwordHasher = passwordHasher;
    }

    public MyProfile getProfile(LoginAccount actor, Long customerId) {
        if (customerId != null && customerId <= 0) {
            throw ApiException.badRequest();
        }
        if (customerId != null) {
            return responseMapper.toMyProfile(accountMapper.findMemberByCustomerId(customerId));
        }
        return getProfile(actor);
    }

    public MyProfile getProfile(LoginAccount actor) {
        return responseMapper.toMyProfile(accountMapper.findMemberById(actor.accountId()));
    }

    @Transactional
    public MyProfile updateProfile(LoginAccount actor, ContactUpdateRequest request) {
        if (!request.hasChanges()) {
            throw ApiException.badRequest();
        }
        customerMapper.updateContact(actor.customerId(), request.phone(), request.email(), request.address(), Instant.now());
        if (request.role() != null) {
            throw ApiException.badRequest();
        }
        customerMapper.updateInternal(actor.customerId(), request.creditStatus(), request.collectionStatus(),
                request.assignedConsultantId(), Instant.now());
        if (request.role() != null || request.status() != null) {
            accountMapper.updateRoleAndStatus(actor.accountId(), request.role(), request.status());
        }
        return getProfile(actor);
    }

    @Transactional
    public void changePassword(LoginAccount actor, PasswordChangeRequest request) {
        Account account = accountMapper.findById(actor.accountId());
        if (!passwordHasher.matches(request.currentPassword(), account.getPasswordHash())) {
            throw ApiException.badRequest();
        }
        accountMapper.updatePassword(actor.accountId(), passwordHasher.hash(request.newPassword()), Instant.now());
        accountMapper.endSessions(actor.accountId());
    }
}
