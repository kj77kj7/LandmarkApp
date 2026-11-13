package project.landmark.controller;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.landmark.dto.ApiResponse;
import project.landmark.dto.LandmarkPosition;
import project.landmark.entity.Landmark;
import project.landmark.entity.User;
import project.landmark.service.LandmarkService;
import project.landmark.service.RankingService;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeApiController {

    private final LandmarkService landmarkService;
    private final RankingService rankingService;

    // 홈 화면 데이터 (로그인 필요)
    @GetMapping
    public ResponseEntity<ApiResponse<HomeResponse>> getHomeData(@AuthenticationPrincipal User user) {

        // 1. 이번 주 내 순위 (순위권 밖이면 0 → 프론트에서 '-' 표시)
        int myRank = rankingService.calculateWeeklyRanking().stream()
                .filter(dto -> dto.getUserId().equals(user.getId()))
                .map(dto -> dto.getRank())
                .findFirst()
                .orElse(0);

        // 2. 현재 위치와 다음 목표 랜드마크
        long currentSteps = LandmarkService.stepsOf(user);
        LandmarkPosition position = landmarkService.getPosition(user);
        Landmark next = position.next();
        String nextName = next != null ? next.getName() : "데이터 없음";
        Long nextRequired = next != null ? next.getRequiredSteps() : 100000L;

        // 3. 응답
        HomeResponse response = HomeResponse.builder()
                .userInfo(HomeResponse.UserInfo.builder()
                        .nickname(user.getNickname())
                        .totalSteps(currentSteps)
                        .representativeTitle(user.getRepresentativeTitle())
                        .build())
                .rankingInfo(HomeResponse.RankingInfo.builder()
                        .rank(myRank)
                        .build())
                .landmarkInfo(HomeResponse.LandmarkInfo.builder()
                        .name(nextName)
                        .requiredSteps(nextRequired)
                        .currentSteps(currentSteps)
                        .build())
                .currentLocationName(position.currentName())
                .build();

        return ResponseEntity.ok(ApiResponse.ok("홈 데이터 조회 성공", response));
    }

    @Getter
    @Builder
    public static class HomeResponse {
        private UserInfo userInfo;
        private RankingInfo rankingInfo;
        private LandmarkInfo landmarkInfo;
        private String currentLocationName;

        @Getter @Builder
        public static class UserInfo {
            private String nickname;
            private Long totalSteps;
            private String representativeTitle;
        }

        @Getter @Builder
        public static class RankingInfo {
            private int rank;
        }

        @Getter @Builder
        public static class LandmarkInfo {
            private String name;
            private Long requiredSteps;
            private Long currentSteps;
        }
    }
}
