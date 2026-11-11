package com.bss.customer.profile;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContactUpdateRequest(
        @Size(max = 20) @Pattern(regexp = ContactRules.PHONE) String phone,
        @Size(max = 100) @Pattern(regexp = ContactRules.EMAIL) String email,
        @Size(max = 200) @Pattern(regexp = ContactRules.NOT_BLANK_TEXT) String address,
        String creditStatus,
        String collectionStatus,
        String assignedConsultantId,
        String role,
        String status) {

    public boolean hasChanges() {
        return phone != null || email != null || address != null;
    }
}
