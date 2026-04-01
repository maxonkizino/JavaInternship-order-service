package com.javainternshiporderservice.client;

import com.javainternshiporderservice.dto.response.UserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${USER_SERVICE_URL}")
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserInfoResponse getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/users/by-email/{email}")
    UserInfoResponse getUserByEmail(@PathVariable("email") String email);

}
