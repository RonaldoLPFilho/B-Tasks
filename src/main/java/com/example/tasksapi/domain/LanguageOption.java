package com.example.tasksapi.domain;

public enum LanguageOption {
    PT_BR("Português Brasileiro"),
    EN_US("English"),
    ES_ES("Español");

    private final String displayName;

    LanguageOption(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
