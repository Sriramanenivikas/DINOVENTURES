package com.dinoventures.wallet.dto.response;

import lombok.*;

import java.util.List;

/**
 * Paginated response wrapper for list endpoints.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
}
