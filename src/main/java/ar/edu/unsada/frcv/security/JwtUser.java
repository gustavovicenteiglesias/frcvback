package ar.edu.unsada.frcv.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

public class JwtUser {
    private final String userId;
    private final String email;
    private final List<String> roles;

    public JwtUser(String userId, String email, List<String> roles) {
        this.userId = userId;
        this.email = email;
        this.roles = roles == null ? List.of() : roles;
    }

    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public List<String> getRoles() { return roles; }

    public Collection<? extends GrantedAuthority> asAuthorities() {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .toList();
    }
}
