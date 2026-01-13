package com.example.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SecurityService {

    private final AuthenticationContext authenticationContext;

    public SecurityService(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public UserDetails getAuthenticatedUser() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class).orElse(null);
    }

    public String getUsername() {
        UserDetails user = getAuthenticatedUser();
        return user != null ? user.getUsername() : null;
    }

    public Set<String> getRoles() {
        UserDetails user = getAuthenticatedUser();
        if (user == null) {
            return Set.of();
        }
        return user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
            .collect(Collectors.toSet());
    }

    public boolean hasRole(String role) {
        return getRoles().contains(role);
    }

    public void logout() {
        authenticationContext.logout();
    }
}
