package project.landmark.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final long VALIDITY_MS = 1000L * 60 * 60; // 1시간

    private final Key key;

    // 서명 키는 설정(jwt.secret / 환경변수 JWT_SECRET)에서 읽습니다.
    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        byte[] bytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "jwt.secret(JWT_SECRET)이 비어 있거나 32바이트보다 짧습니다. application-local.yml을 확인하세요.");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String createToken(Long userId, String username) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + VALIDITY_MS);

        return Jwts.builder()
                .setSubject(username)
                .claim("id", userId)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
