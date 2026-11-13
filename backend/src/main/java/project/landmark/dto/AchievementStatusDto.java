package project.landmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementStatusDto {
    private Long id;
    private String name;
    private String description;
    private String conditionType;
    private Long conditionValue;

    private boolean unlocked;    // 이미 보상을 받았는지(해금 완료)
    private String iconUrl;

    // 조건 달성 여부 (보상 받을 수 있는지)
    private boolean isConditionMet;

    // 조건 설명 텍스트 (예: "하루 20,000보 3회")
    private String conditionText;
}