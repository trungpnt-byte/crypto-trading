package com.aquarius.crypto.controller;

import com.aquarius.crypto.common.LocalApiResponse;
import com.aquarius.crypto.model.User;
import com.aquarius.crypto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/trade/v1/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/public/all")
//    @PreAuthorize("hasRole('TRADER')")
    public Mono<LocalApiResponse<List<User>>> findAll() {
        return Mono.fromCallable(() ->
                        userRepository.findAll(Pageable.unpaged())
                )
                .subscribeOn(Schedulers.boundedElastic())
                .map(users ->
                        LocalApiResponse.<List<User>>builder()
                                .success(true)
                                .data(StreamSupport.stream(users.spliterator(), false).toList())
                                .message("Fetched users successfully")
                                .build()
                );
    }


}
