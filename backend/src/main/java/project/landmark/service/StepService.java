package project.landmark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.landmark.entity.StepRecord;
import project.landmark.entity.User;
import project.landmark.repository.StepRecordRepository;
import project.landmark.repository.UserRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class StepService {

    private final StepRecordRepository stepRecordRepository;
    private final UserRepository userRepository;
    private final LandmarkService landmarkService;

    public void addSteps(String username, int newSteps) {
        if (newSteps <= 0) return; // 0보 이하는 무시

        User user = userRepository.getByUsername(username);

        // 1. 누적 걸음 수 + 레벨 갱신 (한 번만 더함)
        user.addSteps((long) newSteps);

        // 2. 오늘 날짜 기록 갱신
        LocalDate today = LocalDate.now();
        StepRecord record = stepRecordRepository.findByUserAndDate(user, today)
                .orElseGet(() -> stepRecordRepository.save(StepRecord.builder()
                        .user(user)
                        .date(today)
                        .steps(0L)
                        .build()));
        record.setSteps(record.getSteps() + newSteps);

        // 3. 새로 도달한 랜드마크 해금 + 알림
        landmarkService.checkAndUnlockLandmarks(user);
    }
}
