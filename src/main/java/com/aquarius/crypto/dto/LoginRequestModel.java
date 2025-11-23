package com.aquarius.crypto.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequestModel {
    private String email;

    private String password;
}
