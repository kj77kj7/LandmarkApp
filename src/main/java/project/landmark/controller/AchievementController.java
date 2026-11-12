package project.landmark.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import project.landmark.dto.AchievementStatusDto;
import project.landmark.dto.ApiResponse;
import project.landmark.entity.Achievement;
import project.landmark.entity.User;
import project.landmark.service.AchievementService;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    // 업적 목록 + 내 달성 여부 (로그인 필요)
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<AchievementStatusDto>>> getAchievementList(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok("업적 목록 조회 성공",
                achievementService.getAchievementsWithStatus(user.getUsername())));
    }

    // 업적 획득 버튼 (로그인 필요)
    @PostMapping("/{id}/claim")
    public ResponseEntity<ApiResponse<String>> claimAchievement(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        String achievementName = achievementService.claimAchievement(user.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok("업적 획득 성공", achievementName));
    }

    // 전체 업적 목록 (공개)
    @GetMapping
    public ResponseEntity<List<Achievement>> getAchievements() {
        return ResponseEntity.ok(achievementService.getAchievements());
    }

    // 업적 상세 (공개)
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Achievement> getById(@PathVariable Long id) {
        return achievementService.getAchievement(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 특정 사용자가 획득한 업적 (로그인 필요)
    @GetMapping("/user/{username}")
    public ResponseEntity<List<Achievement>> getUserAchievements(@PathVariable String username) {
        return ResponseEntity.ok(achievementService.getUserAchievements(username));
    }
}
