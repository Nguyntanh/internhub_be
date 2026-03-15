package com.example.internhub_be.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.internhub_be.domain.User;
import com.example.internhub_be.exception.ResourceNotFoundException;
import com.example.internhub_be.repository.UserRepository;

import java.util.Collection;

@Service("securityService") // Alias for @PreAuthorize
public class SecurityService {

    private final UserRepository userRepository = null;

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

    public User getCurrentUser() {
        // 1. Lấy đối tượng Authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. Kiểm tra nếu người dùng chưa đăng nhập hoặc là anonymous
        if (authentication == null || !authentication.isAuthenticated() 
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("No user is currently logged in");
        }

        // 3. Lấy email/username từ Principal (trong dự án của bạn thường là email)
        String email = authentication.getName();

        // 4. Truy vấn Database để lấy toàn bộ thông tin User
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
