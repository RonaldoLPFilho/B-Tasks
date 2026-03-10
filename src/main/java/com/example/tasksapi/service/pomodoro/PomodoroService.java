package com.example.tasksapi.service.pomodoro;

import com.example.tasksapi.domain.pomodoro.PomodoroMode;
import com.example.tasksapi.domain.pomodoro.PomodoroPreferences;
import com.example.tasksapi.domain.pomodoro.PomodoroStatus;
import com.example.tasksapi.domain.pomodoro.PomodoroTimerState;
import com.example.tasksapi.domain.User;
import com.example.tasksapi.dto.AcknowledgePomodoroAlarmRequestDTO;
import com.example.tasksapi.dto.CreatePomodoroPreferenceDTO;
import com.example.tasksapi.dto.PomodoroTimerStateDTO;
import com.example.tasksapi.dto.ResetPomodoroTimerRequestDTO;
import com.example.tasksapi.dto.StartPomodoroTimerRequestDTO;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.repository.PomodoroPreferencesRepository;
import com.example.tasksapi.repository.PomodoroTimerStateRepository;
import com.example.tasksapi.service.user.AuthenticatedUserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PomodoroService {
    private final PomodoroPreferencesRepository pomodoroPreferencesRepository;
    private final PomodoroTimerStateRepository pomodoroTimerStateRepository;
    private final PomodoroAlarmSoundService pomodoroAlarmSoundService;
    private final AuthenticatedUserService authenticatedUserService;

    public PomodoroService(
            PomodoroPreferencesRepository pomodoroPreferencesRepository,
            PomodoroTimerStateRepository pomodoroTimerStateRepository,
            PomodoroAlarmSoundService pomodoroAlarmSoundService,
            AuthenticatedUserService authenticatedUserService) {
        this.pomodoroPreferencesRepository = pomodoroPreferencesRepository;
        this.pomodoroTimerStateRepository = pomodoroTimerStateRepository;
        this.pomodoroAlarmSoundService = pomodoroAlarmSoundService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Transactional
    public void createDefaultPomodoroPreferences(User user) {
        PomodoroPreferences pomodoroPreferences = pomodoroPreferencesRepository.findByUserId(user.getId());
        if (pomodoroPreferences == null) {
            pomodoroPreferences = new PomodoroPreferences(
                    user,
                    30,
                    5,
                    "aplausos.wav"
            );
            pomodoroPreferencesRepository.save(pomodoroPreferences);
        }

        createDefaultPomodoroTimerState(user, pomodoroPreferences);
    }

    @Transactional
    public PomodoroPreferences getPomodoroPreferences() {
        User user = authenticatedUserService.getCurrentUser();
        return getOrCreatePomodoroPreferences(user);
    }

    @Transactional
    public PomodoroPreferences updatePomodoroPreferences(CreatePomodoroPreferenceDTO dto) {
        User user = authenticatedUserService.getCurrentUser();
        validatePreferences(dto);

        PomodoroPreferences preferences = getOrCreatePomodoroPreferences(user);
        preferences.setSessionDuration(dto.sessionDuration());
        preferences.setBreakDuration(dto.breakDuration());
        preferences.setAlarmSound(dto.alarmSound());
        PomodoroPreferences savedPreferences = pomodoroPreferencesRepository.save(preferences);

        PomodoroTimerState state = getOrCreatePomodoroTimerState(user, savedPreferences);
        if (state.getStatus() != PomodoroStatus.RUNNING && state.getStatus() != PomodoroStatus.ALARM) {
            state.setRemainingSeconds(getDurationForMode(state.getMode(), savedPreferences));
            state.setStartedAt(null);
            state.setEndsAt(null);
            pomodoroTimerStateRepository.save(state);
        }

        return savedPreferences;
    }

    @Transactional
    public PomodoroTimerStateDTO getPomodoroTimerState() {
        User user = authenticatedUserService.getCurrentUser();
        PomodoroPreferences preferences = getOrCreatePomodoroPreferences(user);
        PomodoroTimerState state = refreshExpiredTimerState(getOrCreatePomodoroTimerState(user, preferences), preferences);
        return toStateDto(state, preferences);
    }

    @Transactional
    public PomodoroTimerStateDTO startTimer(StartPomodoroTimerRequestDTO request) {
        User user = authenticatedUserService.getCurrentUser();
        PomodoroPreferences preferences = getOrCreatePomodoroPreferences(user);
        PomodoroTimerState state = getOrCreatePomodoroTimerState(user, preferences);

        PomodoroMode mode = request != null && request.mode() != null ? request.mode() : PomodoroMode.SESSION;
        int durationInSeconds = getDurationForMode(mode, preferences);

        state.setMode(mode);
        state.setStatus(PomodoroStatus.RUNNING);
        state.setStartedAt(LocalDateTime.now());
        state.setEndsAt(LocalDateTime.now().plusSeconds(durationInSeconds));
        state.setRemainingSeconds(durationInSeconds);
        state.setAlarmAcknowledged(true);

        return toStateDto(pomodoroTimerStateRepository.save(state), preferences);
    }

    @Transactional
    public PomodoroTimerStateDTO pauseTimer() {
        User user = authenticatedUserService.getCurrentUser();
        PomodoroPreferences preferences = getOrCreatePomodoroPreferences(user);
        PomodoroTimerState state = refreshExpiredTimerState(getOrCreatePomodoroTimerState(user, preferences), preferences);

        if (state.getStatus() != PomodoroStatus.RUNNING) {
            throw new InvalidDataException("Pomodoro timer is not running");
        }

        state.setRemainingSeconds(calculateRemainingSeconds(state));
        state.setStatus(PomodoroStatus.PAUSED);
        state.setStartedAt(null);
        state.setEndsAt(null);

        return toStateDto(pomodoroTimerStateRepository.save(state), preferences);
    }

    @Transactional
    public PomodoroTimerStateDTO resumeTimer() {
        User user = authenticatedUserService.getCurrentUser();
        PomodoroPreferences preferences = getOrCreatePomodoroPreferences(user);
        PomodoroTimerState state = refreshExpiredTimerState(getOrCreatePomodoroTimerState(user, preferences), preferences);

        if (state.getStatus() != PomodoroStatus.PAUSED) {
            throw new InvalidDataException("Pomodoro timer is not paused");
        }

        state.setStatus(PomodoroStatus.RUNNING);
        state.setStartedAt(LocalDateTime.now());
        state.setEndsAt(LocalDateTime.now().plusSeconds(state.getRemainingSeconds()));
        state.setAlarmAcknowledged(true);

        return toStateDto(pomodoroTimerStateRepository.save(state), preferences);
    }

    @Transactional
    public PomodoroTimerStateDTO resetTimer(ResetPomodoroTimerRequestDTO request) {
        User user = authenticatedUserService.getCurrentUser();
        PomodoroPreferences preferences = getOrCreatePomodoroPreferences(user);
        PomodoroTimerState state = getOrCreatePomodoroTimerState(user, preferences);

        PomodoroMode mode = request != null && request.mode() != null ? request.mode() : state.getMode();
        if (mode == null) {
            mode = PomodoroMode.SESSION;
        }

        state.setMode(mode);
        state.setStatus(PomodoroStatus.IDLE);
        state.setStartedAt(null);
        state.setEndsAt(null);
        state.setRemainingSeconds(getDurationForMode(mode, preferences));
        state.setAlarmAcknowledged(true);

        return toStateDto(pomodoroTimerStateRepository.save(state), preferences);
    }

    @Transactional
    public PomodoroTimerStateDTO acknowledgeAlarm(AcknowledgePomodoroAlarmRequestDTO request) {
        User user = authenticatedUserService.getCurrentUser();
        PomodoroPreferences preferences = getOrCreatePomodoroPreferences(user);
        PomodoroTimerState state = refreshExpiredTimerState(getOrCreatePomodoroTimerState(user, preferences), preferences);

        if (state.getStatus() != PomodoroStatus.ALARM) {
            throw new InvalidDataException("Pomodoro timer alarm is not active");
        }

        PomodoroMode nextMode = request != null && request.nextMode() != null
                ? request.nextMode()
                : toggleMode(state.getMode());

        int durationInSeconds = getDurationForMode(nextMode, preferences);
        state.setMode(nextMode);
        state.setAlarmAcknowledged(true);

        if (request != null && request.autoStart()) {
            state.setStatus(PomodoroStatus.RUNNING);
            state.setStartedAt(LocalDateTime.now());
            state.setEndsAt(LocalDateTime.now().plusSeconds(durationInSeconds));
            state.setRemainingSeconds(durationInSeconds);
        } else {
            state.setStatus(PomodoroStatus.IDLE);
            state.setStartedAt(null);
            state.setEndsAt(null);
            state.setRemainingSeconds(durationInSeconds);
        }

        return toStateDto(pomodoroTimerStateRepository.save(state), preferences);
    }

    private PomodoroPreferences getOrCreatePomodoroPreferences(User user) {
        PomodoroPreferences preferences = pomodoroPreferencesRepository.findByUserId(user.getId());
        if (preferences != null) {
            return preferences;
        }

        createDefaultPomodoroPreferences(user);
        PomodoroPreferences createdPreferences = pomodoroPreferencesRepository.findByUserId(user.getId());
        if (createdPreferences == null) {
            throw new NotFoundException("Pomodoro preferences not found");
        }
        return createdPreferences;
    }

    private PomodoroTimerState getOrCreatePomodoroTimerState(User user, PomodoroPreferences preferences) {
        return pomodoroTimerStateRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPomodoroTimerState(user, preferences));
    }

    private PomodoroTimerState createDefaultPomodoroTimerState(User user, PomodoroPreferences preferences) {
        PomodoroTimerState state = pomodoroTimerStateRepository.findByUserId(user.getId()).orElse(null);
        if (state != null) {
            return state;
        }

        PomodoroTimerState timerState = new PomodoroTimerState();
        timerState.setUser(user);
        timerState.setMode(PomodoroMode.SESSION);
        timerState.setStatus(PomodoroStatus.IDLE);
        timerState.setStartedAt(null);
        timerState.setEndsAt(null);
        timerState.setRemainingSeconds(getDurationForMode(PomodoroMode.SESSION, preferences));
        timerState.setAlarmAcknowledged(true);
        return pomodoroTimerStateRepository.save(timerState);
    }

    private PomodoroTimerState refreshExpiredTimerState(PomodoroTimerState state, PomodoroPreferences preferences) {
        if (state.getStatus() != PomodoroStatus.RUNNING || state.getEndsAt() == null) {
            return state;
        }

        int remainingSeconds = calculateRemainingSeconds(state);
        if (remainingSeconds > 0) {
            state.setRemainingSeconds(remainingSeconds);
            return state;
        }

        state.setStatus(PomodoroStatus.ALARM);
        state.setStartedAt(null);
        state.setEndsAt(null);
        state.setRemainingSeconds(0);
        state.setAlarmAcknowledged(false);
        return pomodoroTimerStateRepository.save(state);
    }

    private PomodoroTimerStateDTO toStateDto(PomodoroTimerState state, PomodoroPreferences preferences) {
        int expectedSeconds = getDurationForMode(state.getMode(), preferences);
        int remainingSeconds = state.getStatus() == PomodoroStatus.RUNNING
                ? calculateRemainingSeconds(state)
                : state.getRemainingSeconds();

        if (state.getStatus() == PomodoroStatus.IDLE && remainingSeconds <= 0) {
            remainingSeconds = expectedSeconds;
        }

        if (state.getStatus() != PomodoroStatus.RUNNING
                && state.getStatus() != PomodoroStatus.ALARM
                && (remainingSeconds <= 0 || remainingSeconds > expectedSeconds)) {
            remainingSeconds = expectedSeconds;
        }

        return new PomodoroTimerStateDTO(
                state.getId(),
                state.getMode(),
                state.getStatus(),
                state.getStartedAt(),
                state.getEndsAt(),
                Math.max(remainingSeconds, 0),
                state.isAlarmAcknowledged()
        );
    }

    private int calculateRemainingSeconds(PomodoroTimerState state) {
        if (state.getEndsAt() == null) {
            return state.getRemainingSeconds();
        }

        long remaining = Duration.between(LocalDateTime.now(), state.getEndsAt()).getSeconds();
        return (int) Math.max(remaining, 0);
    }

    private int getDurationForMode(PomodoroMode mode, PomodoroPreferences preferences) {
        return switch (mode) {
            case BREAK -> preferences.getBreakDuration() * 60;
            case SESSION -> preferences.getSessionDuration() * 60;
        };
    }

    private PomodoroMode toggleMode(PomodoroMode currentMode) {
        return currentMode == PomodoroMode.BREAK ? PomodoroMode.SESSION : PomodoroMode.BREAK;
    }

    private void validatePreferences(CreatePomodoroPreferenceDTO dto) {
        if (dto.sessionDuration() <= 0) {
            throw new InvalidDataException("Session duration must be greater than zero");
        }

        if (dto.breakDuration() <= 0) {
            throw new InvalidDataException("Break duration must be greater than zero");
        }

        if (dto.alarmSound() == null || dto.alarmSound().isBlank()) {
            throw new InvalidDataException("Alarm sound is required");
        }

        if (!pomodoroAlarmSoundService.isValidSoundFile(dto.alarmSound())) {
            throw new InvalidDataException("Invalid alarm sound");
        }
    }
}
