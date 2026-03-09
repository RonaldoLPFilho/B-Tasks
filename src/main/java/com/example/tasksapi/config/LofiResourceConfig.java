package com.example.tasksapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LofiResourceConfig implements WebMvcConfigurer {

    private final AudioConfig audioConfig;

    public LofiResourceConfig(AudioConfig audioConfig) {
        this.audioConfig = audioConfig;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/lofi/**")
                .addResourceLocations(audioConfig.getLofiRadioResourceLocation());
        registry.addResourceHandler("/alarms/**")
                .addResourceLocations(audioConfig.getAlarmsResourceLocation());
    }
}
