package com.javainternshiporderservice.client;

import com.javainternshiporderservice.dto.response.UserInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceFallback {

    public UserInfoResponse createFallback(String identifier) {
        log.warn("Creating fallback UserInfoResponse for: {}", identifier);
        UserInfoResponse fallback = new UserInfoResponse();
        fallback.setName("Unknown");
        fallback.setSurname("User");
        fallback.setActive(false);
        return fallback;
    }

}
