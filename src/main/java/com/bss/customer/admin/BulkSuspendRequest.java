package com.bss.customer.admin;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record BulkSuspendRequest(@JsonProperty(value = "account_ids", required = true) @NotEmpty @Size(max = 50) List<@NotBlank @Size(max = 64) String> accountIds,
                                 @JsonProperty(value = "reason", required = true) @NotBlank @Size(max = 500) String reason) {
}
