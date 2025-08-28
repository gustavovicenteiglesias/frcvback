package ar.edu.unsada.frcv.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {
    @Value("${app.auth.jwt.secret}")
    private String secret;

    @Value("${app.auth.jwt.issuer}")
    private String issuer;

    @Value("${app.auth.jwt.ttl-seconds}")
    private long ttlSeconds;

    public String issue(String userId, String email, List<String> roles) {
        Instant now = Instant.now();
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(userId)
                .setAudience("frcv-mobile")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
                .claim("email", email)
                .claim("roles", roles)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
