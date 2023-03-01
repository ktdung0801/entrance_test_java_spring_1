package com.management.user.security.service;

import com.management.user.controller.dto.RefreshTokenRequest;
import com.management.user.controller.dto.TokenResponseDto;
import com.management.user.controller.dto.UserAuthenticationResponse;
import com.management.user.controller.dto.UserAuthenticationRequest;
import com.management.user.controller.dto.UserRegisterRequest;
import com.management.user.controller.dto.UserResponseDto;
import com.management.user.entity.Tokens;
import com.management.user.entity.Users;
import com.management.user.entity.enums.Role;
import com.management.user.repository.TokenRepository;
import com.management.user.repository.UsersRepository;
import com.management.user.security.jwt.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersRepository usersRepository;

    private final TokenRepository tokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtils jwtUtils;

    private final AuthenticationManager authenticationManager;

    @Value("${auth.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${auth.app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    public UserResponseDto register(UserRegisterRequest requestUser) throws Exception {

        if (usersRepository.findByEmail(requestUser.getEmail()).isPresent()) {
            throw new Exception("An account is already registered with this email address");
        }

        Users newUser = Users.builder().
                firstName(requestUser.getFirstName())
                .lastName(requestUser.getLastName())
                .email(requestUser.getEmail())
                .password(passwordEncoder.encode(requestUser.getPassword()))
                .role(Role.USER)
                .createAt(Instant.now())
                .build();

        Users savedUser = usersRepository.save(newUser);

        return UserResponseDto.builder()
                .id(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .displayName(savedUser.getFullName())
                .build();
    }

    public UserAuthenticationResponse authenticate(UserAuthenticationRequest requestUser) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestUser.getEmail(),
                        requestUser.getPassword()
                )
        );

       Users user = usersRepository.findByEmail(requestUser.getEmail())
               .orElseThrow();

        String jwtToken = jwtUtils.generateToken(user);
        Optional<Tokens> existedToken = tokenRepository.findTokenByUserId(user.getId());

        Tokens savedToken = existedToken.map(this::updateUserToken).orElseGet(() -> saveUserToken(user));

       return UserAuthenticationResponse.builder()
               .user(
                       UserResponseDto.builder()
                               .firstName(user.getFirstName())
                               .lastName(user.getLastName())
                               .email(user.getEmail())
                               .displayName(user.getFullName())
                               .build()
               )
               .token(
                       TokenResponseDto.builder()
                               .token(jwtToken)
                               .refreshToken(savedToken.getRefreshToken())
                               .build()
               )
               .build();
    }

    @Transactional
    public void logoutUser() {
        Users userDetails = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        tokenRepository.deleteByUserId(userDetails.getId());
    }

    public TokenResponseDto refreshToken(RefreshTokenRequest request) throws Exception {
        String refreshToken = request.getRefreshToken();
        Tokens token = verifyExpiration(refreshToken);

        Users user = usersRepository.findById(token.getUserId())
                .orElseThrow();

        String jwtToken = jwtUtils.generateToken(user);
        Tokens savedToken = updateUserToken(token);

        return TokenResponseDto.builder()
                .token(jwtToken)
                .refreshToken(savedToken.getRefreshToken())
                .build();
    }

    private Tokens saveUserToken(Users user) {
        var token = Tokens.builder()
                .userId(user.getId())
                .createAt(Instant.now())
                .expiresIn(Instant.now().plusMillis(jwtExpirationMs))
                .refreshToken(UUID.randomUUID().toString())
                .refreshExpiresIn(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();
        return tokenRepository.save(token);
    }

    private Tokens updateUserToken(Tokens token) {
        token.setUpdateAt(Instant.now());
        token.setRefreshToken(UUID.randomUUID().toString());
        token.setRefreshExpiresIn(Instant.now().plusMillis(refreshTokenDurationMs));
        return tokenRepository.save(token);
    }

    private Tokens verifyExpiration(String refreshToken) throws Exception {

        Tokens token = tokenRepository.findTokenByRefreshToken(refreshToken).orElseThrow(() ->
                new Exception("Token is not in database")
        );

        if(token.getRefreshExpiresIn().compareTo(Instant.now()) < 0) {
            token.setRefreshToken("");
            tokenRepository.save(token);
            throw new Exception("Refresh token was expired. Please make a new sign-in request");
        }

        return token;
    }
}
