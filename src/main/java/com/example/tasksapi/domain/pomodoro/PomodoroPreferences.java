package com.example.tasksapi.domain.pomodoro;

import com.example.tasksapi.domain.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "pomodoro_preferences")
public class PomodoroPreferences {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    private int sessionDuration;

    private int breakDuration;

    private String alarmSound;

    @OneToOne
    @JsonBackReference
    private User user;

    public PomodoroPreferences(User user, int sessionDuration, int breakDuration, String alarmSound) {
        this.user = user;
        this.sessionDuration = sessionDuration;
        this.breakDuration = breakDuration;
        this.alarmSound = alarmSound;
    }

    public PomodoroPreferences() {}

    public UUID getId() {
        return id;
    }

    public int getSessionDuration() {
        return sessionDuration;
    }

    public void setSessionDuration(int sessionDuration) {
        this.sessionDuration = sessionDuration;
    }

    public int getBreakDuration() {
        return breakDuration;
    }

    public void setBreakDuration(int breakDuration) {
        this.breakDuration = breakDuration;
    }

    public String getAlarmSound() {
        return alarmSound;
    }

    public void setAlarmSound(String alarmSound) {
        this.alarmSound = alarmSound;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
