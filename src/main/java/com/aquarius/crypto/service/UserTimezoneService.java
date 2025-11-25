package com.aquarius.crypto.service;

import com.aquarius.crypto.model.User;
import com.aquarius.crypto.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserTimezoneService {

    private final UserRepository userRepository;

    private final ConcurrentHashMap<Long, ZoneId> timezoneCache = new ConcurrentHashMap<>();

    public UserTimezoneService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Mono<ZoneId> getPreferredZoneId(Long internalUserId) {

        ZoneId cachedZone = timezoneCache.get(internalUserId);
        if (cachedZone != null) {
            return Mono.just(cachedZone);
        }

        return userRepository.findById(internalUserId)
                .map(User::getPreferredTimezone)
                .map(ZoneId::of)
                .defaultIfEmpty(ZoneId.of("UTC"))
                .doOnNext(zone -> timezoneCache.put(internalUserId, zone));
    }
}
