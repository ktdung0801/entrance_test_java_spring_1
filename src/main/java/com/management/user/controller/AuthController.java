package com.management.user.controller;

import com.management.user.controller.dto.RefreshTokenRequest;
import com.management.user.controller.dto.TokenResponseDto;
import com.management.user.controller.dto.UserAuthenticationResponse;
import com.management.user.controller.dto.UserAuthenticationRequest;
import com.management.user.controller.dto.UserRegisterRequest;
import com.management.user.controller.dto.UserResponseDto;
import com.management.user.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDto> saveUser(
            @Valid @RequestBody UserRegisterRequest user
    ) {
        return new ResponseEntity<>(authService.register(user), HttpStatus.CREATED);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<UserAuthenticationResponse> authenticate(
            @Valid @RequestBody UserAuthenticationRequest user
    ) {
        return ResponseEntity.ok(authService.authenticate(user));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponseDto> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/sign-out")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logoutUser () {
        authService.logoutUser();
    }
}
