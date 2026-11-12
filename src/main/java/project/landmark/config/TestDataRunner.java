package project.landmark.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import project.landmark.entity.Achievement;
import project.landmark.entity.User;
import project.landmark.repository.AchievementRepository;
import project.landmark.repository.UserRepository;

import java.util.List;

/** 개발용 초기 데이터: 테스트 유저 1명과 업적 목록 (비어 있을 때만 넣음). prod 프로필에서는 실행하지 않음 */
@Slf4j
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class TestDataRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AchievementRepository achievementRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.save(User.builder()
                    .username("kj77kj")
                    .password(passwordEncoder.encode("12345678"))
                    .email("kj77kj@naver.com")
                    .nickname("경준")
                    .build());
            log.info("테스트용 유저('경준') 생성 완료");
        }

        if (achievementRepository.count() == 0) {
            achievementRepository.saveAll(List.of(
                    achievement("럭키 세븐", "난 살아있는 행운이라고?", "total_steps", 77777L, "lucky_seven"),
                    achievement("반짝이 신발", "내 신발… 아직 살아있니?", "daily_steps_count", 3L, "shiny_shoes"),
                    achievement("꾸준한 자", "오늘도 한 발짝 더.", "consecutive_days", 7L, "steady"),
                    achievement("걸음 수 수집가", "발자국도 모으면 재산이지.", "total_steps", 300000L, "collector"),
                    achievement("마라톤 준비생", "발걸음으로 마라톤 완주했다!", "total_steps", 60000L, "marathon"),
                    achievement("지구 둘레 한 바퀴(?)", "지구 한 바퀴는 힘들지만… 시작은 해봤다.", "total_steps", 500000L, "earth"),
                    achievement("세계일주 기분내기", "여긴 어디 또 어디?", "landmark_count", 5L, "world_tour"),
                    achievement("여행 중독자", "한 번 걷기 시작하면 멈출 수 없지.", "total_steps", 150000L, "addict")
            ));
            log.info("테스트용 업적 데이터 삽입 완료");
        }
    }

    private static Achievement achievement(String name, String description, String conditionType,
                                           long conditionValue, String iconKey) {
        return Achievement.builder()
                .name(name)
                .description(description)
                .conditionType(conditionType)
                .conditionValue(conditionValue)
                .iconUrl(iconKey) // 프론트엔드 아이콘 매핑 키
                .build();
    }
}
