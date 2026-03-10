package com.example.tasksapi.service.user;

import com.example.tasksapi.domain.LanguageOption;
import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.UserLanguagePreference;
import com.example.tasksapi.repository.UserLanguagePreferenceRepository;
import org.springframework.stereotype.Service;

@Service
public class UserLanguagePreferenceService {
    private final UserLanguagePreferenceRepository repository;
    private final AuthenticatedUserService authenticatedUserService;

    public UserLanguagePreferenceService(UserLanguagePreferenceRepository repository,
                                         AuthenticatedUserService authenticatedUserService) {
        this.repository = repository;
        this.authenticatedUserService = authenticatedUserService;
    }

    public void createDefaultLanguagePreferences(User user) {
        UserLanguagePreference userLanguagePreference = new UserLanguagePreference(
                user,
                LanguageOption.ES_ES
        );
        repository.save(userLanguagePreference);
    }

    public UserLanguagePreference getUserLanguagePreference() {
        return repository.getByUserId(authenticatedUserService.getCurrentUser().getId());
    }


    public void updateUserLanguagePreference(LanguageOption language) {
        User user = authenticatedUserService.getCurrentUser();
        UserLanguagePreference preference = repository.getByUserId(user.getId());
        preference.setLanguage(language);
        repository.save(preference);
    }

}
