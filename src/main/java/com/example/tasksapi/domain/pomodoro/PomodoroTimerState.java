package com.example.tasksapi.domain.pomodoro;

import com.example.tasksapi.domain.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pomodoro_timer_state")
public class PomodoroTimerState {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PomodoroMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PomodoroStatus status;

    private LocalDateTime startedAt;

    private LocalDateTime endsAt;

    @Column(nullable = false)
    private Integer remainingSeconds;

    @Column(nullable = false)
    private boolean alarmAcknowledged;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonBackReference
    private User user;

    public UUID getId() {
        return id;
    }

    public PomodoroMode getMode() {
        return mode;
    }

    public void setMode(PomodoroMode mode) {
        this.mode = mode;
    }

    public PomodoroStatus getStatus() {
        return status;
    }

    public void setStatus(PomodoroStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(LocalDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public Integer getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(Integer remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public boolean isAlarmAcknowledged() {
        return alarmAcknowledged;
    }

    public void setAlarmAcknowledged(boolean alarmAcknowledged) {
        this.alarmAcknowledged = alarmAcknowledged;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
