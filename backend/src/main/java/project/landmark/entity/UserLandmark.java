package project.landmark.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_landmark")
public class UserLandmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landmark_id", nullable = false)
    private Landmark landmark;

    private LocalDateTime unlockedAt;

    @PrePersist
    public void prePersist() {
        if (unlockedAt == null) {
            unlockedAt = LocalDateTime.now();
        }
    }
}
