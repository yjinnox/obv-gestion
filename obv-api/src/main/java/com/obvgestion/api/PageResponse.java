package com.obvgestion.api;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Enveloppe de pagination commune (§13) : {@code content, page, size, totalElements, totalPages}. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public PageResponse {
        content = List.copyOf(content);
    }

    public static <S, T> PageResponse<T> de(Page<S> page, Function<S, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
