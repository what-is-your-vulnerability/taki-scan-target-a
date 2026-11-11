package com.bss.customer.signup;

import com.bss.customer.account.AccountStatus;

public record SignupResponse(String signupRef, AccountStatus status) {
}
