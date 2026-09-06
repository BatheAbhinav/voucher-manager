package abhinav.projects.vouchermanager.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
class JwtService {

    private final SecretKey key;
    private final Duration ttl = Duration.ofHours(12);

    JwtService(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    String issue(UUID subjectId, Role role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subjectId.toString())
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    /** Throws io.jsonwebtoken.JwtException or IllegalArgumentException on an invalid/expired token. */
    AuthenticatedUser verify(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        UUID subjectId = UUID.fromString(claims.getSubject());
        Role role = Role.valueOf(claims.get("role", String.class));
        return new AuthenticatedUser(subjectId, role);
    }
}
