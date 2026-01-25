package com.streamit.groupchatapp.security.principal;

import com.streamit.groupchatapp.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record UserPrincipal(
        Long id,
        String email,
        String name,
        String profileImageUrl,

        Collection<? extends GrantedAuthority> authorities
) implements UserDetails {

    // Helper method to convert your JPA User entity to this Principal
    public static UserPrincipal create(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getProfileImageUrl(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return null; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}