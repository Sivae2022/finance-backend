package com.financedashboard.access.user;

import java.time.OffsetDateTime;

public record UserResponse(
        Long id,
        String username,
        String fullName,
        UserRole role,
        UserStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
