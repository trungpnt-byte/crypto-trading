package com.aquarius.crypto.controller;


import com.aquarius.crypto.common.LocalApiResponse;
import com.aquarius.crypto.common.UUIDConverter;
import com.aquarius.crypto.dto.request.LoginRequestModel;
import com.aquarius.crypto.dto.response.AuthResponseModel;
import com.aquarius.crypto.model.UserPrincipal;
import com.aquarius.crypto.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final ReactiveAuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/auth")
    public Mono<LocalApiResponse<AuthResponseModel>> authenticate(
            @RequestBody Mono<LoginRequestModel> loginRequestMono) {

        return loginRequestMono
                .map(login -> new UsernamePasswordAuthenticationToken(
                        login.getEmail(),
                        login.getPassword()
                ))
                .flatMap(authenticationManager::authenticate)
                .flatMap(auth -> {
                    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
                    return Mono.just(principal);
                })
                .map(principal -> {
                    String accessToken = jwtService.generateAccessToken(principal);
                    String refreshToken = jwtService.generateRefreshToken(principal);

                    return AuthResponseModel.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .publicId(UUIDConverter.uuidToString(principal.publicId()))
                            .build();
                })
                .map(authResponse -> LocalApiResponse.success(
                        authResponse,
                        "Authentication successful",
                        HttpStatus.OK.value()
                ))
                .onErrorResume(e -> {
                    String message = "Authentication failed: Invalid credentials.";
                    if (e instanceof RuntimeException) {
                        message = "Authentication failed: " + e.getMessage();
                    }
                    return Mono.just(LocalApiResponse.error(message, HttpStatus.UNAUTHORIZED.value()));
                });
    }
}
