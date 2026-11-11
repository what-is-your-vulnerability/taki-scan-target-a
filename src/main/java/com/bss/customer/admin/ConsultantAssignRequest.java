package com.bss.customer.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConsultantAssignRequest(
        @JsonProperty(value = "consultant_account_id", required = true) String consultantAccountId) {
}
