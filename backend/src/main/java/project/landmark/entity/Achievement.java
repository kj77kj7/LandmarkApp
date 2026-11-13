package project.landmark.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 업적명
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    // 업적 설명
    @Column(nullable = false, length = 200)
    private String description;

    // 조건 타입 (total_steps, daily_steps_count, consecutive_days, landmark_count, early_morning_walks)
    @Column(nullable = false)
    private String conditionType;

    // 조건 값
    @Column(nullable = false)
    private Long conditionValue;

    // 프론트엔드 아이콘 매핑 키
    private String iconUrl;
}
