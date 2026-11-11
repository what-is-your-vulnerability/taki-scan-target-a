package com.bss.customer.auth;

import org.springframework.http.HttpStatus;

import com.bss.customer.common.ApiException;

public class LoginFailedException extends ApiException {

    public LoginFailedException() {
        super(HttpStatus.UNAUTHORIZED, "LOGIN_FAILED", "아이디 또는 비밀번호를 확인해 주세요.");
    }
}
