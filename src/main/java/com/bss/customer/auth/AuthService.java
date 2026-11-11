package com.bss.customer.auth;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bss.customer.account.Account;
import com.bss.customer.account.AccountMapper;
import com.bss.customer.account.AccountStatus;
import com.bss.customer.crypto.PasswordHasher;

@Service
public class AuthService {

    private final AccountMapper accountMapper;
    private final PasswordHasher passwordHasher;
    private final int lockThreshold;

    public AuthService(AccountMapper accountMapper, PasswordHasher passwordHasher,
                       @Value("${app.login.lock-threshold}") int lockThreshold) {
        this.accountMapper = accountMapper;
        this.passwordHasher = passwordHasher;
        this.lockThreshold = lockThreshold;
    }

    public Account login(String loginId, String password) {
        Account account = loginId == null ? null : accountMapper.findByLoginId(loginId);
        if (account == null) {
            passwordHasher.matchAgainstPlaceholder(password);
            throw handleLoginFailure(null);
        }
        if (!passwordHasher.matches(password, account.getPasswordHash())) {
            throw handleLoginFailure(account);
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new LoginFailedException();
        }
        accountMapper.recordLoginSuccess(account.getAccountId(), Instant.now());
        return account;
    }

    public void lockIfExceeded(String loginId, int recentFailures) {
        if (loginId == null || recentFailures < lockThreshold) {
            return;
        }
        Account account = accountMapper.findByLoginId(loginId);
        if (account != null && account.getStatus() == AccountStatus.ACTIVE) {
            accountMapper.updateStatus(account.getAccountId(), AccountStatus.LOCKED);
        }
    }

    public void logout(String accountId) {
        accountMapper.endSessions(accountId);
    }

    private LoginFailedException handleLoginFailure(Account account) {
        return new LoginFailedException();
    }
}
