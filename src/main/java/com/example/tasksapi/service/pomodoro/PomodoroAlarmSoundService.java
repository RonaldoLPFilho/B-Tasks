package com.example.tasksapi.service.pomodoro;

import com.example.tasksapi.domain.pomodoro.PomodoroSoundOption;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Comparator;

@Service
public class PomodoroAlarmSoundService {
    private static final String ALARMS_PATTERN = "classpath:/static/alarms/*";

    public List<PomodoroSoundOption> listAvailableSounds() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        try {
            Resource[] resources = resolver.getResources(ALARMS_PATTERN);

            return java.util.Arrays.stream(resources)
                    .map(Resource::getFilename)
                    .filter(Objects::nonNull)
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
