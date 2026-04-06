package com.financedashboard.access.auth;

import com.financedashboard.access.user.UserRole;
import com.financedashboard.access.user.UserStatus;

public record AuthUserResponse(
        Long id,
        String username,
        String fullName,
        UserRole role,
        UserStatus status
) {
}
