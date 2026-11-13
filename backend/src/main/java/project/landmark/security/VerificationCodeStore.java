package project.landmark.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 이메일 인증번호 저장소 (아이디 찾기, 비밀번호 재설정 공용).
 * - 6자리 숫자, SecureRandom으로 생성
 * - 5분 후 만료
 * - 5번 틀리면 무효화
 * 서버 메모리에 보관하므로 재시작하면 사라집니다. (운영 환경에서는 Redis 권장)
 */
@Component
public class VerificationCodeStore {

    private static final Duration TTL = Duration.ofMinutes(5);
    private static final int MAX_ATTEMPTS = 5;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    private record Entry(String code, Instant expiresAt, AtomicInteger failures) {}

    /** 새 인증번호를 만들어 저장하고 돌려줍니다. 같은 key의 이전 번호는 덮어씁니다. */
    public String issue(String key) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        store.put(key, new Entry(code, Instant.now().plus(TTL), new AtomicInteger()));
        return code;
    }

    /** 인증번호를 확인합니다. 맞으면 번호를 소모하고, 틀리면 IllegalArgumentException을 던집니다. */
    public void verify(String key, String input) {
        Entry entry = store.get(key);
        if (entry == null) {
            throw new IllegalArgumentException("인증번호를 먼저 요청해 주세요.");
        }
        if (Instant.now().isAfter(entry.expiresAt())) {
            store.remove(key);
            throw new IllegalArgumentException("인증번호가 만료되었습니다. 다시 요청해 주세요.");
        }
        boolean matches = input != null && MessageDigest.isEqual(
                entry.code().getBytes(StandardCharsets.UTF_8), input.getBytes(StandardCharsets.UTF_8));
        if (!matches) {
            if (entry.failures().incrementAndGet() >= MAX_ATTEMPTS) {
                store.remove(key);
                throw new IllegalArgumentException("인증번호를 " + MAX_ATTEMPTS + "번 틀려 무효화되었습니다. 다시 요청해 주세요.");
            }
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }
        store.remove(key);
    }
}
