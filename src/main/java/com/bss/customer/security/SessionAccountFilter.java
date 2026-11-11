package com.bss.customer.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bss.customer.account.Account;
import com.bss.customer.account.AccountMapper;
import com.bss.customer.account.AccountStatus;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class SessionAccountFilter extends OncePerRequestFilter {

    private final AccountMapper accountMapper;

    public SessionAccountFilter(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Account account = loadSessionAccount(session);
            if (account == null) {
                session.invalidate();
            } else {
                authenticate(account);
            }
        }
        chain.doFilter(request, response);
    }

    private Account loadSessionAccount(HttpSession session) {
        String accountId = (String) session.getAttribute(SessionKeys.ACCOUNT_ID);
        Integer epoch = (Integer) session.getAttribute(SessionKeys.SESSION_EPOCH);
        if (accountId == null || epoch == null) {
            return null;
        }
        Account account = accountMapper.findById(accountId);
        if (account == null || account.getSessionEpoch() != epoch) {
            return null;
        }
        return account;
    }

    private void authenticate(Account account) {
        LoginAccount principal = new LoginAccount(account.getAccountId(), account.getCustomerId(), account.getRole());
        var authority = new SimpleGrantedAuthority("ROLE_" + account.getRole().name());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of(authority)));
        SecurityContextHolder.setContext(context);
    }
}
