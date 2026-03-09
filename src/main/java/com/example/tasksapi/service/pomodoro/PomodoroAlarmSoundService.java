package com.example.tasksapi.service.pomodoro;

import com.example.tasksapi.domain.pomodoro.PomodoroSoundOption;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PomodoroAlarmSoundService {

    public List<PomodoroSoundOption> listAvailableSounds() {
        List <PomodoroSoundOption> sounds = new ArrayList<>();

        sounds.add(new PomodoroSoundOption("Aplausos", "aplausos.wav", "/alarms/aplausos.wav"));
        sounds.add(new PomodoroSoundOption("Alarme Suspense", "alarm-suspense.wav", "/alarms/alarm-suspense.wav"));
        sounds.add(new PomodoroSoundOption("Epic Orquestra", "epic-orchestra-transition.wav", "/alarms/epic-orchestra-transition.wav"));
        sounds.add(new PomodoroSoundOption("Retro Game", "retro-game-emergency-alarm.wav", "/alarms/retro-game-emergency-alarm.wav"));
        sounds.add(new PomodoroSoundOption("Tick Tock", "tick-tock-clock-timer.wav", "/alarms/tick-tock-clock-timer.wav"));


        return sounds;
    }

    public boolean isValidSoundFile(String fileName) {
        return listAvailableSounds().stream()
                .anyMatch(sound -> sound.file().equals(fileName));
    }

}
