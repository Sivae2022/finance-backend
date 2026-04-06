package com.financedashboard.access.auth;

import com.financedashboard.access.security.FinanceUserPrincipal;
import com.financedashboard.access.user.AppUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public AuthUserResponse currentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof FinanceUserPrincipal principal)) {
            throw new IllegalArgumentException("No authenticated user is available");
        }

        AppUser user = principal.getUser();
        return new AuthUserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getStatus()
        );
    }
}
