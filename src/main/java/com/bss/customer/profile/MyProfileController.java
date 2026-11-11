package com.bss.customer.profile;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bss.customer.security.LoginAccount;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/me")
public class MyProfileController {

    private final MyProfileService myProfileService;

    public MyProfileController(MyProfileService myProfileService) {
        this.myProfileService = myProfileService;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public MyProfile getProfile(@AuthenticationPrincipal LoginAccount actor,
                                @org.springframework.web.bind.annotation.RequestParam(name = "customer_id", required = false) Long customerId) {
        return myProfileService.getProfile(actor, customerId);
    }

    @PatchMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public MyProfile updateProfile(@AuthenticationPrincipal LoginAccount actor,
                                   @Valid @RequestBody ContactUpdateRequest request) {
        return myProfileService.updateProfile(actor, request);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal LoginAccount actor,
                               @Valid @RequestBody PasswordChangeRequest request,
                               HttpServletRequest httpRequest) {
        myProfileService.changePassword(actor, request);
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
