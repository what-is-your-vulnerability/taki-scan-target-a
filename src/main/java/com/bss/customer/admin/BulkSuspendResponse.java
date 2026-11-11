package com.bss.customer.admin;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BulkSuspendResponse(@JsonProperty("suspended") List<String> suspended,
                                  @JsonProperty("skipped") List<String> skipped) {
}
