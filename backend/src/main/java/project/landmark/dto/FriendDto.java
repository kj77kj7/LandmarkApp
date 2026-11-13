package project.landmark.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class FriendDto {
    private Long id;
    private String friendNickname;
    private String status;

    // 친구 프로필 표시에 필요한 상세 정보들
    private String currentLandmark;   // 현재 위치
    private Long totalSteps;          // 총 걸음 수
    private String profileImageUrl;   // 프로필 사진
    private String representativeTitle; // 칭호
    private String statusMessage;     // 상태 메시지
}