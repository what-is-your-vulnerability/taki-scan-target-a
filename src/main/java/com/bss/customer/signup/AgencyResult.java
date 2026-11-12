package com.bss.customer.signup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgencyResult(String outcome, String verificationId, String name, String birthDate, String ci) {
}
