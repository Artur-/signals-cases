package com.example.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.signals.ValueSignal;
import com.vaadin.signals.WritableSignal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Application-scoped signal holding the current authenticated user information.
 * This signal is reactive to authentication changes including login, logout, and impersonation.
 */
@Component
public class CurrentUserSignal {

    public static class UserInfo {
        private final String username;
        private final Set<String> roles;
        private final boolean authenticated;

        public UserInfo(String username, Set<String> roles, boolean authenticated) {
            this.username = username;
            this.roles = roles;
            this.authenticated = authenticated;
        }

        public static UserInfo anonymous() {
            return new UserInfo(null, Set.of(), false);
        }

        public static UserInfo authenticated(String username, Set<String> roles) {
            return new UserInfo(username, roles, true);
        }

        public String getUsername() {
            return username;
        }

        public Set<String> getRoles() {
            return roles;
        }

        public boolean isAuthenticated() {
            return authenticated;
        }

        public boolean hasRole(String role) {
            return roles.contains(role);
        }

        public boolean hasAnyRole(String... roles) {
            for (String role : roles) {
                if (this.roles.contains(role)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            if (!authenticated) {
                return "Anonymous";
            }
            return username + " [" + String.join(", ", roles) + "]";
        }
    }

    private final WritableSignal<UserInfo> userSignal;
    private final AuthenticationContext authenticationContext;

    public CurrentUserSignal(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
        this.userSignal = new ValueSignal<>(getCurrentUserInfo());
    }

    /**
     * Get the signal holding current user information.
     * This signal updates when authentication state changes.
     */
    public WritableSignal<UserInfo> getUserSignal() {
        return userSignal;
    }

    /**
     * Refresh the user signal from the current authentication context.
     * Call this after login, logout, or impersonation changes.
     */
    public void refresh() {
        userSignal.value(getCurrentUserInfo());
    }

    private UserInfo getCurrentUserInfo() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
            .map(user -> {
                Set<String> roles = new java.util.HashSet<>(authenticationContext.getGrantedRoles());
                return UserInfo.authenticated(user.getUsername(), roles);
            })
            .orElse(UserInfo.anonymous());
    }
}
