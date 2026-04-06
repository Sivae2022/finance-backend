package com.financedashboard.access.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 80, message = "Username must be at most 80 characters")
        String username,
        @NotBlank(message = "Full name is required")
        @Size(max = 120, message = "Full name must be at most 120 characters")
        String fullName,
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 120, message = "Password must be between 8 and 120 characters")
        String password,
        @NotNull(message = "Role is required")
        UserRole role,
        @NotNull(message = "Status is required")
        UserStatus status
) {
}
