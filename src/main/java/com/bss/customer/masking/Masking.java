package com.bss.customer.masking;

import java.time.LocalDate;

public final class Masking {

    private Masking() {
    }

    public static String name(String value) {
        if (value == null) {
            return null;
        }
        int[] chars = value.codePoints().toArray();
        if (chars.length <= 1) {
            return "*";
        }
        StringBuilder masked = new StringBuilder().appendCodePoint(chars[0]);
        if (chars.length == 2) {
            return masked.append('*').toString();
        }
        masked.append("*".repeat(chars.length - 2));
        return masked.appendCodePoint(chars[chars.length - 1]).toString();
    }

    public static String birthDate(LocalDate value) {
        return value == null ? null : "%04d-**-**".formatted(value.getYear());
    }

    public static String phone(String value) {
        if (value == null) {
            return null;
        }
        if (!value.contains("-")) {
            return value;
        }
        String digits = value.replaceAll("\\D", "");
        if (digits.length() < 10) {
            return "***";
        }
        return digits.substring(0, 3) + "-" + digits.substring(3, digits.length() - 4) + "-****";
    }

    public static String email(String value) {
        if (value == null) {
            return null;
        }
        int at = value.indexOf('@');
        if (at < 1) {
            return "***";
        }
        String local = value.substring(0, at);
        if (local.length() <= 2) {
            return value;
        }
        String domain = value.substring(at);
        return local.length() <= 2 ? local.charAt(0) + "*" + domain : local.substring(0, 2) + "***" + domain;
    }

    public static String address(String value) {
        if (value == null) {
            return null;
        }
        String[] tokens = value.trim().split("\\s+");
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < Math.min(2, tokens.length - 1); i++) {
            masked.append(tokens[i]).append(' ');
        }
        return masked.append("***").toString();
    }

    public static String residentRegNo(String digits) {
        if (digits == null) {
            return null;
        }
        return digits.substring(0, 6) + "-" + digits.charAt(6) + "******";
    }
}
