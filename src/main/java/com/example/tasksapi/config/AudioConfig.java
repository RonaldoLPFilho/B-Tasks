package com.example.tasksapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class AudioConfig {

    private final String basePath;

    public AudioConfig(@Value("${tasks.audio.base-path}") String basePath) {
        this.basePath = basePath.replaceAll("/+$", "");
    }

    public String getBasePath() {
        return basePath;
    }

    public String getAlarmsResourceLocation() {
        return "file:" + basePath + "/alarms/";
    }

    public String getLofiRadioResourceLocation() {
        return "file:" + basePath + "/lofi-radio/";
    }

    public Path getAlarmsPath() {
        return Path.of(basePath, "alarms");
    }

    public Path getLofiRadioPath() {
        return Path.of(basePath, "lofi-radio");
    }
}
