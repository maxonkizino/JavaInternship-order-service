package com.javainternshiporderservice.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileConfigTest {

    @Mock
    private Environment environment;

    private ProfileConfig profileConfig;

    @BeforeEach
    void setUp() {
        profileConfig = new ProfileConfig(environment);
    }

    @Test
    void isProfileActive_shouldReturnTrue_whenProfileIsActive() {
        when(environment.matchesProfiles("test")).thenReturn(true);

        boolean result = profileConfig.isProfileActive("test");

        assertThat(result).isTrue();
    }

    @Test
    void isProfileActive_shouldReturnFalse_whenProfileIsNotActive() {
        when(environment.matchesProfiles("prod")).thenReturn(false);

        boolean result = profileConfig.isProfileActive("prod");

        assertThat(result).isFalse();
    }

    @Test
    void getActiveProfile_shouldReturnFirstActiveProfile() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test", "local"});

        String result = profileConfig.getActiveProfile();

        assertThat(result).isEqualTo("test");
    }

    @Test
    void getActiveProfile_shouldReturnDefault_whenNoProfilesActive() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{});

        String result = profileConfig.getActiveProfile();

        assertThat(result).isEqualTo("default");
    }

    @Test
    void getActiveProfile_shouldHandleSingleProfile() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

        String result = profileConfig.getActiveProfile();

        assertThat(result).isEqualTo("prod");
    }

    @Test
    void isProfileActive_shouldHandleLocalProfile() {
        when(environment.matchesProfiles("local")).thenReturn(true);

        assertThat(profileConfig.isProfileActive("local")).isTrue();
    }

    @Test
    void isProfileActive_shouldHandleProdProfile() {
        when(environment.matchesProfiles("prod")).thenReturn(true);

        assertThat(profileConfig.isProfileActive("prod")).isTrue();
    }

    @Test
    void isProfileActive_shouldHandleDevProfile() {
        when(environment.matchesProfiles("dev")).thenReturn(true);

        assertThat(profileConfig.isProfileActive("dev")).isTrue();
    }
}
