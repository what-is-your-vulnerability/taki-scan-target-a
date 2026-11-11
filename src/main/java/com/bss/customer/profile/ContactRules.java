package com.bss.customer.profile;

public final class ContactRules {

    public static final String PHONE = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$";
    public static final String EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    public static final String NOT_BLANK_TEXT = "(?s).*\\S.*";

    private ContactRules() {
    }
}
