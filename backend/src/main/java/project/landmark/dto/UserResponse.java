package project.landmark.dto;

import lombok.*;
import project.landmark.entity.User;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private Long totalSteps;
    private String representativeTitle;
    private int level;
    private String profileImageUrl;
    private String statusMessage;

    // 현재 위치한 랜드마크 이름
    private String currentLandmark;

    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .totalSteps(user.getTotalSteps())
                .representativeTitle(user.getRepresentativeTitle())
                .level(user.getLevel())
                .profileImageUrl(user.getProfileImageUrl())
                .statusMessage(user.getStatusMessage())
                .createdAt(user.getCreatedAt())
                // currentLandmark는 Service/Controller에서 계산해서 넣어야 하므로 여기선 null 또는 따로 처리
                .build();
    }
}