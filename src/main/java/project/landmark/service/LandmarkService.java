package project.landmark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.landmark.dto.LandmarkPosition;
import project.landmark.entity.*;
import project.landmark.repository.LandmarkRepository;
import project.landmark.repository.NotificationRepository;
import project.landmark.repository.UserLandmarkRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LandmarkService {

    private final LandmarkRepository landmarkRepository;
    private final UserLandmarkRepository userLandmarkRepository;
    private final NotificationRepository notificationRepository;

    /** 걸음 수 오름차순으로 정렬된 전체 랜드마크 */
    @Transactional(readOnly = true)
    public List<Landmark> findAllSorted() {
        return landmarkRepository.findAllByOrderByRequiredStepsAsc();
    }

    /** 사용자의 현재 위치와 다음 목표 랜드마크 */
    @Transactional(readOnly = true)
    public LandmarkPosition getPosition(User user) {
        return LandmarkPosition.of(findAllSorted(), stepsOf(user));
    }

    /** 사용자가 지금 있는 랜드마크 이름 */
    @Transactional(readOnly = true)
    public String getCurrentLandmarkName(User user) {
        return getPosition(user).currentName();
    }

    /** 새로 도달한 랜드마크를 해금 기록하고 알림을 보냅니다. (걸음 수 저장 시 호출) */
    public void checkAndUnlockLandmarks(User user) {
        long currentSteps = stepsOf(user);

        for (Landmark lm : landmarkRepository.findAll()) {
            long required = lm.getRequiredSteps() != null ? lm.getRequiredSteps() : 0L;
            if (currentSteps < required || userLandmarkRepository.existsByUserAndLandmark(user, lm)) {
                continue;
            }
            userLandmarkRepository.save(UserLandmark.builder()
                    .user(user)
                    .landmark(lm)
                    .unlockedAt(LocalDateTime.now())
                    .build());
            notificationRepository.save(Notification.of(user, NotificationType.LANDMARK,
                    lm.getName() + " 랜드마크가 해금되었습니다!", null));
        }
    }

    public static long stepsOf(User user) {
        return user.getTotalSteps() != null ? user.getTotalSteps() : 0L;
    }
}
