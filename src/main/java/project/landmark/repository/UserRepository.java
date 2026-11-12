package project.landmark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.landmark.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    // 아이디 찾기용
    Optional<User> findByNicknameAndEmail(String nickname, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    /** 아이디로 사용자를 찾고, 없으면 IllegalArgumentException을 던집니다. */
    default User getByUsername(String username) {
        return findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
