package com.aquarius.crypto.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.aquarius.crypto.service.UserTimezoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Custom Jackson Serializer that converts UTC Instant to the user's preferred ZonedDateTime.
 * This is ONLY used if the business requires localized time rendering on the server side.
 */
@Component
public class LocalizedInstantSerializer extends StdSerializer<Instant> {

    private static final DateTimeFormatter LOCALIZED_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private final UserTimezoneService timezoneService;

    public LocalizedInstantSerializer() {
        this(null);
    }


    @Autowired
    public LocalizedInstantSerializer(UserTimezoneService timezoneService) {
        super(Instant.class);
        this.timezoneService = timezoneService;
    }

    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        Mono<ZoneId> zoneMono = ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    Authentication authentication = securityContext.getAuthentication();
                    if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof String userIdStr) {
                        try {
                            Long internalUserId = Long.valueOf(userIdStr);
                            return timezoneService.getPreferredZoneId(internalUserId);
                        } catch (NumberFormatException e) {
                            return Mono.just(ZoneId.of("UTC"));
                        }
                    }
                    return Mono.just(ZoneId.of("UTC"));
                })
                .defaultIfEmpty(ZoneId.of("UTC"));

        ZoneId preferredZone = zoneMono.block();
        ZonedDateTime userLocalTime = ZonedDateTime.ofInstant(value, Objects.requireNonNull(preferredZone));
        gen.writeString(userLocalTime.format(LOCALIZED_FORMATTER));
    }
}