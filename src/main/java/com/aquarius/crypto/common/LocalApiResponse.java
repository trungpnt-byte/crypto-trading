package com.aquarius.crypto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocalApiResponse<T> {
    private boolean success;
    private int status;
    private T data;
    private String message;
    private ZonedDateTime timestamp;

    public static <T> LocalApiResponse<T> success(T data, String message, int status) {
        return LocalApiResponse.<T>builder()
                .success(true)
                .status(status)
                .message(message)
                .data(data)
                .timestamp(ZonedDateTime.now())
                .build();
    }

    public static <T> LocalApiResponse<T> error(String message, int status) {
        return LocalApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .timestamp(ZonedDateTime.now())
                .build();
    }
}
