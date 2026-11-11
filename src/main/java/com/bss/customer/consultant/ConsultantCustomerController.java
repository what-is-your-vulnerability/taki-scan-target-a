package com.bss.customer.consultant;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bss.customer.common.PageResponse;
import com.bss.customer.profile.ContactUpdateRequest;
import com.bss.customer.security.LoginAccount;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/consultant/customers")
public class ConsultantCustomerController {

    private final ConsultantCustomerService consultantCustomerService;

    public ConsultantCustomerController(ConsultantCustomerService consultantCustomerService) {
        this.consultantCustomerService = consultantCustomerService;
    }

    @GetMapping
    @PreAuthorize("hasRole('CONSULTANT')")
    public PageResponse<CustomerListItem> list(@AuthenticationPrincipal LoginAccount actor,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return consultantCustomerService.list(actor, page, size);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('CONSULTANT')")
    public PageResponse<CustomerListItem> search(@AuthenticationPrincipal LoginAccount actor,
                                                 @RequestParam(required = false) String name,
                                                 @RequestParam(required = false) String phone,
                                                 @RequestParam(defaultValue = "customer_id,asc") String sort,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return consultantCustomerService.search(actor, name, phone, sort, page, size);
    }

    @GetMapping("/{customer_id}/profile")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ConsultantCustomerView getProfile(@AuthenticationPrincipal LoginAccount actor,
                                             @PathVariable("customer_id") long customerId) {
        return consultantCustomerService.getProfile(actor, customerId);
    }

    @PatchMapping("/{customer_id}/contact")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ConsultantCustomerView updateContact(@AuthenticationPrincipal LoginAccount actor,
                                                @PathVariable("customer_id") long customerId,
                                                @Valid @RequestBody ContactUpdateRequest request) {
        return consultantCustomerService.updateContact(actor, customerId, request);
    }
}
