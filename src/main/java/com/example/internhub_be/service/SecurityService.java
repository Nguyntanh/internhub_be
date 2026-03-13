package com.example.internhub_be.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service("securityService") // Alias for @PreAuthorize
public class SecurityService {

    public boolean hasPermission(String funcCode, String action) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // 2. If User has role 'ADMIN', return true by default.
        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        // 3. Check if the User's Authorities list contains the string 'funcCode:action'
        String requiredAuthority = funcCode + ":" + action.toUpperCase(); // Ensure action is uppercase for consistency
        return authorities.stream().anyMatch(a -> a.getAuthority().equals(requiredAuthority));
    }
}
