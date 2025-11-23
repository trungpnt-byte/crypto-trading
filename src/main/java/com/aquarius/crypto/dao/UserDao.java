package com.aquarius.crypto.dao;


import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
                    "trader1",
                    "$2a$12$hhUp7igUmXnd2RUWRmoG5uY1df2Ceo6tvzkgJWfdsTt3BkhzhZhqC",
                    Collections.singleton(new SimpleGrantedAuthority("TRADER"))
            ),
            new User(
                    "trader2",
                    "$2a$12$hhUp7igUmXnd2RUWRmoG5uY1df2Ceo6tvzkgJWfdsTt3BkhzhZhqC",
                    Collections.singleton(new SimpleGrantedAuthority("TRADER"))
            ));

    private final static ConcurrentHashMap<String, UserDetails> users = new ConcurrentHashMap<>() {{
        for (UserDetails userDetails : APPLICATION_USERS) {
            put(userDetails.getUsername(), userDetails);
        }
    }};


    public UserDetails findUserByEmail(String email) {
        UserDetails userDetails = users.get(email);
        if (userDetails == null) {
            throw new UsernameNotFoundException("User with email " + email + " not found");
        }
        return userDetails;
    }
}
