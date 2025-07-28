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
        sounds.add(new PomodoroSoundOption("Epic Orquestra", "epic-orchestra.wav", "/alarms/epic-orchestra.wav"));
        sounds.add(new PomodoroSoundOption("Retro game alarme", "retro-game-emergency-alarm.wav", "/alarms/retro-game-emergency-alarm.wav"));
        sounds.add(new PomodoroSoundOption("Tick Tock mané", "tick-tock-clock-timer.wav", "/alarms/tick-tock-clock-timer.wav"));


        return sounds;
    }

}
