package com.hermes.approvalservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public UserInfo getUserById(Long id) {
        log.warn("User service is unavailable. Returning fallback for user id: {}", id);
        UserInfo fallback = new UserInfo();
        fallback.setId(id);
        fallback.setName("Unknown User");
        fallback.setEmail("unknown@example.com");
        fallback.setIsAdmin(false);
        return fallback;
    }

    @Override
    public Boolean isAdmin(Long id) {
        log.warn("User service is unavailable. Returning fallback admin check for user id: {}", id);
        return false;
    }
}