package com.bss.customer.signup;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/signup")
public class SignupController {

    private final SignupService signupService;
    private final IdentityVerificationService verificationService;

    public SignupController(SignupService signupService, IdentityVerificationService verificationService) {
        this.signupService = signupService;
        this.verificationService = verificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
        return signupService.signup(request);
    }

    @PostMapping("/identity-verifications")
    @ResponseStatus(HttpStatus.CREATED)
    public VerificationResponse requestVerification(@Valid @RequestBody VerificationRequest request) {
        return verificationService.request(request.signupRef());
    }

    @PostMapping("/identity-verifications/{verification_id}/complete")
    public VerificationCompleteResponse completeVerification(@PathVariable("verification_id") String verificationId,
                                                             @Valid @RequestBody VerificationCompleteRequest request) {
        return verificationService.complete(verificationId, request.agencyResultRef());
    }
}
