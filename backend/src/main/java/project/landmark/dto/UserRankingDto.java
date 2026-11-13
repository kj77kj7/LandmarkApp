package project.landmark.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRankingDto {

    private Long userId;
    private String nickname;
    private Long totalSteps;
    private int rank;
    private String representativeTitle;
    private String currentLandmark; // 👈 현재 위치 필드

    // 생성자 (필드 6개)
    public UserRankingDto(Long userId, String nickname, Long totalSteps, int rank, String representativeTitle, String currentLandmark) {
        this.userId = userId;
        this.nickname = nickname;
        this.totalSteps = totalSteps;
        this.rank = rank;
        this.representativeTitle = representativeTitle;
        this.currentLandmark = currentLandmark;
    }
}