package dev.dsa.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtil {

    public static final int DEFAULT_PAGE_SIZE = 11;
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * Create pageable with default page size
     */
    public static Pageable createPageable(int page) {
        return createPageable(page, DEFAULT_PAGE_SIZE, null);
    }

    /**
     * Create pageable with custom page size
     */
    public static Pageable createPageable(int page, int size) {
        return createPageable(page, size, null);
    }

    /**
     * Create pageable with custom page size and sorting
     */
    public static Pageable createPageable(int page, int size, Sort sort) {
        // Ensure page is not negative
        int validPage = Math.max(0, page);

        // Ensure size is within valid range
        int validSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);

        if (sort != null) {
            return PageRequest.of(validPage, validSize, sort);
        }
        return PageRequest.of(validPage, validSize);
    }

    /**
     * Create pageable with user preference page size (allows developer override)
     */
    public static Pageable createPageableWithUserPreference(int page, Integer userPageSize, Integer overridePageSize) {
        int size = DEFAULT_PAGE_SIZE;

        // Developer override takes highest priority
        if (overridePageSize != null && overridePageSize > 0) {
            size = overridePageSize;
        }
        // User preference is second priority
        else if (userPageSize != null && userPageSize > 0) {
            size = userPageSize;
        }

        return createPageable(page, size);
    }

    /**
     * Calculate total pages
     */
    public static int calculateTotalPages(long totalElements, int pageSize) {
        return (int) Math.ceil((double) totalElements / pageSize);
    }
}
