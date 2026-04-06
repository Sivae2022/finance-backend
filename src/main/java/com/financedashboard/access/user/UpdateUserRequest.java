package com.financedashboard.access.user;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 120, message = "Full name must be at most 120 characters")
        String fullName,
        @Size(min = 8, max = 120, message = "Password must be between 8 and 120 characters")
        String password,
        UserRole role,
        UserStatus status
) {
}
