package com.example.internhub_be.service;

import com.example.internhub_be.domain.Function;
import com.example.internhub_be.domain.RolePermission;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.repository.FunctionRepository;
import com.example.internhub_be.repository.RolePermissionRepository;
import com.example.internhub_be.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final FunctionRepository functionRepository;

    public UserDetailsServiceImpl(UserRepository userRepository,
                                  RolePermissionRepository rolePermissionRepository,
                                  FunctionRepository functionRepository) {
        this.userRepository = userRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.functionRepository = functionRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("User with email: " + email + " is not active.");
        }

        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add base role authority (e.g., ROLE_ADMIN)
        String roleName = user.getRole().getName();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));

        // Dynamically load granular permissions from RolePermission matrix
        List<RolePermission> rolePermissions = rolePermissionRepository.findById_RoleId(user.getRole().getId());

        for (RolePermission rp : rolePermissions) {
            // Fetch Function to get its code
            Function function = functionRepository.findById(rp.getId().getFunctionId())
                    .orElseThrow(() -> new RuntimeException("Function not found for ID: " + rp.getId().getFunctionId()));

            String functionCode = function.getCode();

            if (rp.getCanAccess()) {
                authorities.add(new SimpleGrantedAuthority(functionCode + ":READ"));
            }
            if (rp.getCanCreate()) {
                authorities.add(new SimpleGrantedAuthority(functionCode + ":CREATE"));
            }
            if (rp.getCanEdit()) {
                authorities.add(new SimpleGrantedAuthority(functionCode + ":UPDATE"));
            }
            if (rp.getCanDelete()) {
                authorities.add(new SimpleGrantedAuthority(functionCode + ":DELETE"));
            }
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}
