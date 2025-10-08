package com.example.tasksapi.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "user_language_preferences")
public class UserLanguagePreference {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LanguageOption language;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public UserLanguagePreference() {}

    public UserLanguagePreference(User user, LanguageOption language) {
        this.user = user;
        this.language = language;
    }

    public UUID getId() {
        return id;
    }


    public LanguageOption getLanguage() {
        return language;
    }

    public void setLanguage(LanguageOption language) {
        this.language = language;
    }

    public User getUser() {
        return user;
    }
}
