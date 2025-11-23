package com.aquarius.crypto.controller;


import com.aquarius.crypto.common.LocalApiResponse;
import com.aquarius.crypto.dao.UserDao;
import com.aquarius.crypto.dto.LoginRequestModel;
import com.aquarius.crypto.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final UserDao userDao;

    private final JwtUtil jwtUtils;

    @PostMapping()
    public LocalApiResponse<String> authenticate(
            @RequestBody LoginRequestModel loginRequestModel
    ) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestModel.getEmail(),
                        loginRequestModel.getPassword()
                )
        );

        final UserDetails userDetails = userDao.findUserByEmail(loginRequestModel.getEmail());
        if (userDetails != null) {
            return LocalApiResponse.success(jwtUtils.generateAccessToken(userDetails), "Authentication successful", 200);

        }

        return LocalApiResponse.error("Authentication failed", 401);
    }
}
