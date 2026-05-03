package com.example.tasksapi.service.howtodo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tasks.howtodo.storage")
public class HowToDoStorageProperties {
    private String provider = "local";
    private String basePath = "/run/media/ronis/Hdzada/MyTasks-MD";
    private long maxFileSizeBytes = 10485760;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }
}
