package project.landmark.dto;

import lombok.Builder;
import lombok.Getter;
import project.landmark.entity.Notification;
import project.landmark.entity.NotificationType;

import java.time.LocalDateTime;

/**
 * 알림 응답. 엔티티를 그대로 내보내면 받는 사람(User)의 비밀번호 해시까지 직렬화되므로 DTO로 변환합니다.
 * JSON 필드 이름은 기존 응답과 같습니다 (isRead → "read").
 */
@Getter
@Builder
public class NotificationDto {
    private Long id;
    private NotificationType type;
    private String message;
    private String senderName;
    private String senderProfileImage;
    private boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationDto from(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .type(n.getType())
                .message(n.getMessage())
                .senderName(n.getSenderName())
                .senderProfileImage(n.getSenderProfileImage())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
