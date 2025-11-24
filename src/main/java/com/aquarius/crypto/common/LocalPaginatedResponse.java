package com.aquarius.crypto.common;

import java.util.List;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocalPaginatedResponse<T> {
    int page;
    int size;
    int totalPages;
    private List<T> contents;
    private long totalItems;
}
