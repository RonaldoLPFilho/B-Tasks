package com.example.tasksapi.service.user;

import com.example.tasksapi.domain.LanguageOption;
import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.UserLanguagePreference;
import com.example.tasksapi.repository.UserLanguagePreferenceRepository;
import com.example.tasksapi.utils.UserUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UserLanguagePreferenceService {
    private final UserLanguagePreferenceRepository repository;

    public UserLanguagePreferenceService(UserLanguagePreferenceRepository repository) {
        this.repository = repository;
    }

    public void createDefaultLanguagePreferences(User user) {
        UserLanguagePreference userLanguagePreference = new UserLanguagePreference(
                user,
                LanguageOption.ES_ES
        );
    }

    public UserLanguagePreference getUserLanguagePreference() {
        return repository.getByUserId(UserUtils.getCurrentUser().getId());
    }


    public void updateUserLanguagePreference(LanguageOption language) {
        User user = UserUtils.getCurrentUser();
        UserLanguagePreference preference = repository.getByUserId(user.getId());
        preference.setLanguage(language);
        repository.save(preference);
    }

}
