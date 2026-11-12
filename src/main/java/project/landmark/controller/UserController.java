package project.landmark.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import project.landmark.dto.ApiResponse;
import project.landmark.dto.UserResponse;
import project.landmark.entity.User;
import project.landmark.service.LandmarkService;
import project.landmark.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final LandmarkService landmarkService;

    // 내 정보 (로그인 필요)
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserResponse>> getUserInfo(@AuthenticationPrincipal User user) {
        UserResponse response = UserResponse.from(user);
        response.setCurrentLandmark(landmarkService.getCurrentLandmarkName(user));
        return ResponseEntity.ok(ApiResponse.ok("유저 정보 조회 성공", response));
    }

    @GetMapping("/titles")
    public ResponseEntity<ApiResponse<List<String>>> getMyTitles(@AuthenticationPrincipal User user) {
        List<String> titles = userService.getAvailableTitles(user.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("칭호 목록 조회 성공", titles));
    }

    @PutMapping("/title")
    public ResponseEntity<ApiResponse<String>> updateTitle(
            @AuthenticationPrincipal User user, @RequestBody Map<String, String> request
    ) {
        String newTitle = request.get("title");
        userService.updateRepresentativeTitle(user.getUsername(), newTitle);
        return ResponseEntity.ok(ApiResponse.ok("칭호 변경 성공", newTitle));
    }

    @PutMapping("/nickname")
    public ResponseEntity<ApiResponse<String>> updateNickname(
            @AuthenticationPrincipal User user, @RequestBody Map<String, String> request
    ) {
        String newNickname = request.get("nickname");
        userService.updateNickname(user.getUsername(), newNickname);
        return ResponseEntity.ok(ApiResponse.ok("닉네임 변경 성공", newNickname));
    }

    @PutMapping("/image")
    public ResponseEntity<ApiResponse<String>> updateImage(
            @AuthenticationPrincipal User user, @RequestBody Map<String, String> request
    ) {
        String imageUrl = request.get("imageUrl");
        userService.updateProfileImage(user.getUsername(), imageUrl);
        return ResponseEntity.ok(ApiResponse.ok("프로필 이미지 변경 성공", imageUrl));
    }

    @PutMapping("/status-message")
    public ResponseEntity<ApiResponse<String>> updateStatusMessage(
            @AuthenticationPrincipal User user, @RequestBody Map<String, String> request
    ) {
        String newMessage = request.get("message");
        userService.updateStatusMessage(user.getUsername(), newMessage);
        return ResponseEntity.ok(ApiResponse.ok("상태 메시지 변경 성공", newMessage));
    }
}
