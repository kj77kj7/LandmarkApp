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
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 알림을 받는 사람

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String message;

    private String senderName;

    // 이미지 데이터가 길 수 있으므로 LONGTEXT
    @Column(columnDefinition = "LONGTEXT")
    private String senderProfileImage;

    private boolean isRead;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 읽지 않은 새 알림을 만듭니다.
     *
     * @param receiver 알림을 받을 사람
     * @param sender   보낸 사람 (시스템 알림이면 null)
     */
    public static Notification of(User receiver, NotificationType type, String message, User sender) {
        return Notification.builder()
                .user(receiver)
                .type(type)
                .message(message)
                .senderName(sender != null ? sender.getNickname() : null)
                .senderProfileImage(sender != null ? sender.getProfileImageUrl() : null)
                .isRead(false)
                .build();
    }
}
