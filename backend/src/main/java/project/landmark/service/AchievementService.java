package project.landmark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.landmark.dto.AchievementStatusDto;
import project.landmark.entity.*;
import project.landmark.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final NotificationRepository notificationRepository;
    private final StepRecordRepository stepRecordRepository;
    private final UserLandmarkRepository userLandmarkRepository;

    // 전체 업적 + 내 달성 현황 (조건값 오름차순)
    @Transactional(readOnly = true)
    public List<AchievementStatusDto> getAchievementsWithStatus(String username) {
        User user = userRepository.getByUsername(username);

        return achievementRepository.findAll().stream()
                .map(ach -> {
                    boolean unlocked = userAchievementRepository.existsByUserAndAchievement_Id(user, ach.getId());
                    return AchievementStatusDto.builder()
                            .id(ach.getId())
                            .name(ach.getName())
                            .description(ach.getDescription())
                            .conditionType(ach.getConditionType())
                            .conditionValue(ach.getConditionValue())
                            .iconUrl(ach.getIconUrl())
                            .unlocked(unlocked)
                            .isConditionMet(unlocked || checkCondition(user, ach))
                            .conditionText(generateConditionText(ach))
                            .build();
                })
                .sorted(Comparator.comparing(AchievementStatusDto::getConditionValue))
                .toList();
    }

    // 업적 획득 (Claim): 조건을 서버에서 다시 확인
    public String claimAchievement(String username, Long achievementId) {
        User user = userRepository.getByUsername(username);
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new IllegalArgumentException("업적을 찾을 수 없습니다."));

        if (userAchievementRepository.existsByUserAndAchievement_Id(user, achievementId)) {
            throw new IllegalArgumentException("이미 획득한 업적입니다.");
        }
        if (!checkCondition(user, achievement)) {
            throw new IllegalArgumentException("아직 조건을 달성하지 못했습니다.");
        }

        userAchievementRepository.save(UserAchievement.builder()
                .user(user)
                .achievement(achievement)
                .achievedAt(LocalDateTime.now())
                .build());
        notificationRepository.save(Notification.of(user, NotificationType.ACHIEVEMENT,
                achievement.getName() + " 업적을 달성하셨습니다!", null));

        return achievement.getName();
    }

    @Transactional(readOnly = true)
    public List<Achievement> getUserAchievements(String username) {
        User user = userRepository.getByUsername(username);
        return userAchievementRepository.findByUser(user).stream()
                .map(UserAchievement::getAchievement)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Achievement> getAchievements() {
        return achievementRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Achievement> getAchievement(Long id) {
        return achievementRepository.findById(id);
    }

    private String generateConditionText(Achievement ach) {
        long val = ach.getConditionValue();
        return switch (ach.getConditionType()) {
            case "total_steps" -> "누적 " + String.format("%,d", val) + "보 달성 시";
            case "daily_steps_count" -> "하루 20,000보 이상 " + val + "회 달성 시";
            case "consecutive_days" -> val + "일 연속 5,000보 이상 달성 시";
            case "landmark_count" -> "랜드마크 " + val + "곳 해금 시";
            case "early_morning_walks" -> "오전 6~10시 8,000보 이상 " + val + "회 달성 시";
            default -> "조건 달성 시";
        };
    }

    private boolean checkCondition(User user, Achievement ach) {
        long targetValue = ach.getConditionValue();

        switch (ach.getConditionType()) {
            case "total_steps":
                return LandmarkService.stepsOf(user) >= targetValue;

            case "daily_steps_count": // 하루 2만 보 N회
                return countDaysWithAtLeast(user, 20000) >= targetValue;

            case "consecutive_days": { // N일 연속 5천 보
                int maxStreak = 0;
                int currentStreak = 0;
                LocalDate prevDate = null;
                for (StepRecord r : stepRecordRepository.findAllByUserOrderByDateAsc(user)) {
                    if (r.getSteps() >= 5000) {
                        if (prevDate == null || r.getDate().isEqual(prevDate.plusDays(1))) {
                            currentStreak++;
                        } else if (!r.getDate().isEqual(prevDate)) {
                            currentStreak = 1;
                        }
                        maxStreak = Math.max(maxStreak, currentStreak);
                        prevDate = r.getDate();
                    } else {
                        currentStreak = 0;
                        prevDate = null;
                    }
                }
                return maxStreak >= targetValue;
            }

            case "landmark_count":
                return userLandmarkRepository.countByUser(user) >= targetValue;

            case "early_morning_walks": // 걸음 시각을 저장하지 않아서 "하루 8,000보 이상 걸은 날" 수로 판정
                return countDaysWithAtLeast(user, 8000) >= targetValue;

            default:
                return false;
        }
    }

    private long countDaysWithAtLeast(User user, long steps) {
        return stepRecordRepository.findAllByUser(user).stream()
                .filter(r -> r.getSteps() >= steps)
                .count();
    }
}
