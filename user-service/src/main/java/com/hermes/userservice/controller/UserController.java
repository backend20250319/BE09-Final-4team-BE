package com.hermes.userservice.controller;

import com.hermes.userservice.dto.UserAuthInfo;
import com.hermes.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/auth/{email}")
    public UserAuthInfo getUserAuthInfo(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }

    @PostMapping("/{userId}/last-login")
    public void updateLastLogin(@PathVariable Long userId) {
        userService.updateLastLogin(userId);
    }

    @PostMapping("/{email}/update-password")
    public void updatePasswordToBcrypt(@PathVariable String email, @RequestParam String plainPassword) {
        userService.updatePasswordToBcrypt(email, plainPassword);
    }
}