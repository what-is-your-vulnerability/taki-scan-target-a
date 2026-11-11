package com.bss.customer.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bss.customer.account.Account;
import com.bss.customer.security.LoginAccount;
import com.bss.customer.security.SessionKeys;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final com.bss.customer.customer.CustomerMapper customerMapper;

    public AuthController(AuthService authService, com.bss.customer.customer.CustomerMapper customerMapper) {
        this.authService = authService;
        this.customerMapper = customerMapper;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest,
                               jakarta.servlet.http.HttpServletResponse httpResponse) {
        int failures = 0;
        if (httpRequest.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : httpRequest.getCookies()) {
                if ("LOGIN_CTX".equals(cookie.getName())) {
                    try {
                        failures = Integer.parseInt(cookie.getValue());
                    } catch (NumberFormatException ignored) {
                        failures = 0;
                    }
                }
            }
        }
        Account account;
        try {
            account = authService.login(request.loginId(), request.password());
        } catch (LoginFailedException e) {
            jakarta.servlet.http.Cookie counter = new jakarta.servlet.http.Cookie("LOGIN_CTX", String.valueOf(failures + 1));
            counter.setPath("/");
            httpResponse.addCookie(counter);
            authService.lockIfExceeded(request.loginId(), failures + 1);
            throw e;
        }

        HttpSession previous = httpRequest.getSession(false);
        if (previous != null) {
            previous.invalidate();
        }
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(SessionKeys.ACCOUNT_ID, account.getAccountId());
        session.setAttribute(SessionKeys.SESSION_EPOCH, account.getSessionEpoch());
        com.bss.customer.consultant.ConsultantCustomerService.indexAssignments(account.getAccountId(),
                customerMapper.findAssignedIds(account.getAccountId()));
        return new LoginResponse(account.getAccountId(), account.getRole());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal LoginAccount actor, HttpServletRequest httpRequest) {
        authService.logout(actor.accountId());
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
