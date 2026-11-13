package project.landmark.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import project.landmark.dto.ApiResponse;
import project.landmark.dto.UserRankingDto;
import project.landmark.entity.User;
import project.landmark.service.RankingService;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    // 주간 랭킹 (최근 7일)
    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<List<UserRankingDto>>> getWeeklyRanking() {
        return ResponseEntity.ok(ApiResponse.ok("주간 랭킹 조회 성공", rankingService.calculateWeeklyRanking()));
    }

    // 월간 랭킹 (이번 달)
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<List<UserRankingDto>>> getMonthlyRanking() {
        return ResponseEntity.ok(ApiResponse.ok("월간 랭킹 조회 성공", rankingService.calculateMonthlyRanking()));
    }

    // 구별 랭킹
    @GetMapping("/district/{district}")
    public ResponseEntity<ApiResponse<List<UserRankingDto>>> getDistrictRanking(@PathVariable String district) {
        return ResponseEntity.ok(ApiResponse.ok(district + " 랭킹 조회 성공", rankingService.calculateDistrictRanking(district)));
    }

    // 친구 랭킹: 로그인 필요, 본인 것만 조회 가능
    @GetMapping("/friends/{username}")
    public ResponseEntity<ApiResponse<List<UserRankingDto>>> getFriendRanking(
            @AuthenticationPrincipal User user,
            @PathVariable String username
    ) {
        if (!user.getUsername().equals(username)) {
            throw new AccessDeniedException("본인의 친구 랭킹만 볼 수 있습니다.");
        }
        return ResponseEntity.ok(ApiResponse.ok(username + "의 친구 랭킹 조회 성공",
                rankingService.calculateFriendRankingByUsername(username)));
    }
}
