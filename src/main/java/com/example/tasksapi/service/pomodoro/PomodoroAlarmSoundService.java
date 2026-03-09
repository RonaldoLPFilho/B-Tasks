package com.example.tasksapi.service.pomodoro;

import com.example.tasksapi.config.AudioConfig;
import com.example.tasksapi.domain.pomodoro.PomodoroSoundOption;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
public class PomodoroAlarmSoundService {

    private final AudioConfig audioConfig;

    public PomodoroAlarmSoundService(AudioConfig audioConfig) {
        this.audioConfig = audioConfig;
    }

    public List<PomodoroSoundOption> listAvailableSounds() {
        Path alarmsPath = audioConfig.getAlarmsPath();

        if (!Files.isDirectory(alarmsPath)) {
            return List.of();
        }

        try (Stream<Path> paths = Files.list(alarmsPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(this::isSupportedAudioFile)
                    .sorted(Comparator.naturalOrder())
                    .map(fileName -> new PomodoroSoundOption(
                            formatDisplayName(fileName),
                            fileName,
                            "/alarms/" + fileName
                    ))
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Could not load pomodoro alarm sounds", e);
        }
    }

    public boolean isValidSoundFile(String fileName) {
        return listAvailableSounds().stream()
                .anyMatch(sound -> sound.file().equals(fileName));
    }

    private boolean isSupportedAudioFile(String fileName) {
        String normalized = fileName.toLowerCase(Locale.ROOT);
        return normalized.endsWith(".mp3") || normalized.endsWith(".wav") || normalized.endsWith(".ogg");
    }

    private String formatDisplayName(String fileName) {
        String withoutExtension = fileName.replaceFirst("\\.[^.]+$", "");
        String normalized = withoutExtension.replace('-', ' ').replace('_', ' ');
        String[] words = normalized.split("\\s+");
        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append(' ');
            }

            if (word.length() == 1) {
                builder.append(word.toUpperCase(Locale.ROOT));
                continue;
            }

            builder.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1));
        }

        return builder.toString();
    }

}
