package ar.edu.unsada.frcv.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${app.auth.jwt.secret}")
    private String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                var jws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
                var c = jws.getBody();

                var principal = new JwtUser(
                        c.getSubject(),                      // sub = userId
                        (String) c.get("email"),
                        (java.util.List<String>) c.get("roles")
                );

                var authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, principal.asAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ignored) {
                // token inválido: dejamos la request sin autenticación
            }
        }
        chain.doFilter(req, res);
    }
}
