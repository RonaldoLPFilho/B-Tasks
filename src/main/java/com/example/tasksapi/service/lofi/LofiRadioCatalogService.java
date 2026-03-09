package com.example.tasksapi.service.lofi;

import com.example.tasksapi.dto.LofiRadioDTO;
import com.example.tasksapi.dto.LofiTrackDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LofiRadioCatalogService {

    public List<LofiRadioDTO> listRadios() {
        return List.of(
                new LofiRadioDTO(
                        "lofi-brasileiro",
                        "Lofi Brasileiro",
                        "brasileiro",
                        List.of(
                                track("lofi-brasileiro-1", "Lofi Brasileiro 1", "brasileiro", "LoFi-brasileiro-1.mp3", 1),
                                track("lofi-brasileiro-2", "Lofi Brasileiro 2", "brasileiro", "LoFi-brasileiro-2.mp3", 2)
                        )
                ),
                new LofiRadioDTO(
                        "lofi-hiphop",
                        "Lofi HipHop",
                        "hiphop",
                        List.of(
                                track("lofi-hiphop-1", "Lofi HipHop 1", "hiphop", "LoFi-HipHop-1.mp3", 1)
                        )
                ),
                new LofiRadioDTO(
                        "radio-mix",
                        "Radio Mix",
                        "radiomix",
                        List.of(
                                track("radio-mix-1", "Radio Mix 1", "radiomix", "RadioMix-1.mp3", 1),
                                track("radio-mix-2", "Radio Mix 2", "radiomix", "RadioMix-2.mp3", 2),
                                track("radio-mix-3", "Radio Mix 3", "radiomix", "RadioMix-3.mp3", 3),
                                track("radio-mix-4", "Radio Mix 4", "radiomix", "RadioMix-4.mp3", 4)
                        )
                )
        );
    }

    private LofiTrackDTO track(String id, String title, String folder, String file, int order) {
        return new LofiTrackDTO(
                id,
                title,
                file,
                "/lofi/" + folder + "/" + file,
                order
        );
    }
}
