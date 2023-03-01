package com.management.user.security.service;

import com.management.user.controller.dto.RefreshTokenRequest;
import com.management.user.controller.dto.TokenResponseDto;
import com.management.user.controller.dto.UserAuthenticationRequest;
import com.management.user.controller.dto.UserAuthenticationResponse;
import com.management.user.controller.dto.UserRegisterRequest;
import com.management.user.controller.dto.UserResponseDto;

public interface AuthServiceInterface {

    UserResponseDto register(UserRegisterRequest requestUser);

    UserAuthenticationResponse authenticate(UserAuthenticationRequest requestUser);

    void logoutUser();

    TokenResponseDto refreshToken(RefreshTokenRequest request);
}
