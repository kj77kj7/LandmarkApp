package project.landmark.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // 친구 요청·삭제가 닉네임으로 상대를 찾으므로 중복되면 안 됩니다.
    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Column(length = 30)
    private String district;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private Long totalSteps = 0L;

    private String representativeTitle;

    @Builder.Default
    private int level = 1;

    @Column(columnDefinition = "LONGTEXT")
    private String profileImageUrl;

    private String statusMessage;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.totalSteps == null) this.totalSteps = 0L;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** 누적 걸음 수를 더하고 레벨을 다시 계산합니다 (1만 보마다 1레벨). */
    public void addSteps(Long newSteps) {
        if (this.totalSteps == null) this.totalSteps = 0L;
        this.totalSteps += newSteps;
        checkLevelUp();
    }

    private void checkLevelUp() {
        int newLevel = (int) (this.totalSteps / 10000) + 1;
        if (newLevel > this.level) {
            this.level = newLevel;
        }
    }
}
