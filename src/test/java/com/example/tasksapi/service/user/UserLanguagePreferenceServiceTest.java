package com.example.tasksapi.service.user;

import com.example.tasksapi.domain.LanguageOption;
import com.example.tasksapi.domain.User;
import com.example.tasksapi.domain.UserLanguagePreference;
import com.example.tasksapi.repository.UserLanguagePreferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserLanguagePreferenceServiceTest {

    @Mock
    private UserLanguagePreferenceRepository repository;
    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private UserLanguagePreferenceService service;

    @Test
    void shouldPersistDefaultLanguagePreference() {
        User user = new User("ronis", "ronis@example.com", "encoded");

        service.createDefaultLanguagePreferences(user);

        ArgumentCaptor<UserLanguagePreference> captor = ArgumentCaptor.forClass(UserLanguagePreference.class);
        verify(repository).save(captor.capture());
        assertEquals(LanguageOption.ES_ES, captor.getValue().getLanguage());
        assertEquals(user, captor.getValue().getUser());
    }

    @Test
    void shouldUpdateCurrentUserLanguagePreference() {
        User user = withId(new User("ronis", "ronis@example.com", "encoded"));
        UserLanguagePreference preference = new UserLanguagePreference(user, LanguageOption.ES_ES);

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(repository.getByUserId(user.getId())).thenReturn(preference);

        service.updateUserLanguagePreference(LanguageOption.PT_BR);

        assertEquals(LanguageOption.PT_BR, preference.getLanguage());
        verify(repository).save(preference);
    }

    private User withId(User user) {
        try {
            java.lang.reflect.Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, java.util.UUID.randomUUID());
            return user;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
