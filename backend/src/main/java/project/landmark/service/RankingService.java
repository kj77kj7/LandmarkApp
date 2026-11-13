package project.landmark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.landmark.dto.LandmarkPosition;
import project.landmark.dto.UserRankingDto;
import project.landmark.entity.Landmark;
import project.landmark.entity.User;
import project.landmark.repository.FriendRepository;
import project.landmark.repository.StepRecordRepository;
import project.landmark.repository.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {

    /** 주간 = 오늘 포함 최근 7일 (BETWEEN은 양 끝을 포함하므로 6일 전부터) */
    private static final int WEEK_DAYS_BEFORE_TODAY = 6;

    private final StepRecordRepository stepRecordRepository;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final LandmarkService landmarkService;

    // 주간 랭킹
    public List<UserRankingDto> calculateWeeklyRanking() {
        LocalDate end = LocalDate.now();
        return toRanking(stepRecordRepository.sumStepsByUserBetweenDates(end.minusDays(WEEK_DAYS_BEFORE_TODAY), end));
    }

    // 월간 랭킹 (이번 달 1일부터)
    public List<UserRankingDto> calculateMonthlyRanking() {
        LocalDate end = LocalDate.now();
        return toRanking(stepRecordRepository.sumStepsByUserBetweenDates(end.withDayOfMonth(1), end));
    }

    // 구별 주간 랭킹
    public List<UserRankingDto> calculateDistrictRanking(String district) {
        LocalDate end = LocalDate.now();
        return toRanking(stepRecordRepository.sumStepsByDistrictBetweenDates(
                end.minusDays(WEEK_DAYS_BEFORE_TODAY), end, district));
    }

    // 친구 주간 랭킹 (나 포함)
    public List<UserRankingDto> calculateFriendRankingByUsername(String username) {
        User user = userRepository.getByUsername(username);

        Set<Long> memberIds = new HashSet<>();
        memberIds.add(user.getId());
        friendRepository.findFriendsByUser(user).forEach(f -> memberIds.add(f.getId()));

        LocalDate end = LocalDate.now();
        List<Object[]> filtered = stepRecordRepository
                .sumStepsByUserBetweenDates(end.minusDays(WEEK_DAYS_BEFORE_TODAY), end).stream()
                .filter(row -> memberIds.contains(((User) row[0]).getId()))
                .toList();

        return toRanking(filtered);
    }

    /** [User, 걸음 합계] 목록(이미 걸음 수 내림차순)을 순위 DTO로 변환 */
    private List<UserRankingDto> toRanking(List<Object[]> rows) {
        List<Landmark> landmarks = landmarkService.findAllSorted();
        List<UserRankingDto> list = new ArrayList<>();
        int rank = 1;

        for (Object[] row : rows) {
            User user = (User) row[0];
            Long periodSteps = row[1] != null ? (Long) row[1] : 0L;
            String nickname = user.getNickname() != null ? user.getNickname() : "알 수 없음";
            String currentLandmark = LandmarkPosition.of(landmarks, LandmarkService.stepsOf(user)).currentName();

            list.add(new UserRankingDto(
                    user.getId(),
                    nickname,
                    periodSteps,
                    rank++,
                    user.getRepresentativeTitle(),
                    currentLandmark
            ));
        }
        return list;
    }
}
