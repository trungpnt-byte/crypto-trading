package com.aquarius.crypto.model;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.Persistable;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User implements Persistable<Long>, BeforeConvertCallback<User> {
    @Id
    private Long id;
    private String username;
    private String password;
    private String email;
    private Instant createdAt;
    private Instant updatedAt;
    private String simpleRole;
    private String tenantId;
    private UUID publicId;

    //TODO: not working as JPA's EntityListeners
    @NotNull
    @Override
    public Publisher<User> onBeforeConvert(User entity, @NotNull SqlIdentifier table) {
        if (entity.getPublicId() == null) {
            entity.setPublicId(UUID.randomUUID());
        }
        return Mono.just(entity);
    }

    @Override
    public boolean isNew() {
        return id == null;
    }
}