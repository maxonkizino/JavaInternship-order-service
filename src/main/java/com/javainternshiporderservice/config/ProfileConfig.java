package com.javainternshiporderservice.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

/**
 * Configuration class for Spring profiles.
 * Logs active profiles on application startup.
 */
@Slf4j
@Configuration
public class ProfileConfig {

    private final Environment environment;

    @Value("${app.name}")
    private String appName;

    @Value("${app.version:unknown}")
    private String appVersion;

    public ProfileConfig(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void logActiveProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();

        if (activeProfiles.length == 0) {
            log.info("No explicit profile set, using default profile");
        } else {
            log.info("Application '{}' version '{}' started with profile(s): {}",
                    appName,
                    appVersion,
                    Arrays.toString(activeProfiles));
        }

        if (isProfileActive("prod")) {
            log.info("Running in PRODUCTION mode");
        } else if (isProfileActive("local")) {
            log.warn("Running in LOCAL mode - Security is relaxed!");
        } else if (isProfileActive("dev")) {
            log.info("Running in DEVELOPMENT mode");
        } else if (isProfileActive("test")) {
            log.info("Running in TEST mode");
        }
    }

    /**
     * Check if a specific profile is active
     */
    public boolean isProfileActive(String profile) {
        return environment.matchesProfiles(profile);
    }

    /**
     * Get the first active profile, or "default" if none
     */
    public String getActiveProfile() {
        String[] profiles = environment.getActiveProfiles();
        return profiles.length > 0 ? profiles[0] : "default";
    }

}
