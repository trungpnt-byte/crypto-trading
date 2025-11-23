package com.aquarius.crypto.dao;


import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class UserDao {

    private final static List<UserDetails> APPLICATION_USERS = List.of(
            new User(
                    "abc",
                    "123",
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))
            ),
            new User(
                    "admin@test.com",
                    "password",
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))
            ),
            new User(
                    "user@test.com",
                    "password",
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
            ),
            new User(
                    "user@test.com",
                    "admin123",
                    Collections.singleton(new SimpleGrantedAuthority("TRADER"))
            ));

    public UserDetails findUserByEmail(String email) {
        return APPLICATION_USERS.stream()
                .filter(userDetails -> userDetails.getUsername().equals(email))
                .findFirst()
                .orElseThrow(
                        () -> new UsernameNotFoundException(
                                "User with username %s does not exist".formatted(email)
                        )
                );
    }
}
