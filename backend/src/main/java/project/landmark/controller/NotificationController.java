package project.landmark.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import project.landmark.dto.ApiResponse;
import project.landmark.dto.NotificationDto;
import project.landmark.entity.User;
import project.landmark.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 내 알림 목록
    @GetMapping
    public ResponseEntity<ApiResponse<Slice<NotificationDto>>> getNotifications(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok("알림 조회 성공",
                notificationService.getMyNotifications(user.getUsername(), page, size)));
    }

    // 안 읽은 알림 개수
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@AuthenticationPrincipal User user) {
        long count = notificationService.getUnreadCount(user.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("안 읽은 알림 개수", count));
    }

    // 알림 삭제 (내 알림만)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteNotification(@PathVariable Long id, @AuthenticationPrincipal User user) {
        notificationService.deleteNotification(id, user.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("알림 삭제 성공", null));
    }

    // 친구 요청 수락 (내게 온 요청만)
    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<String>> acceptFriend(@PathVariable Long id, @AuthenticationPrincipal User user) {
        notificationService.acceptFriendRequest(id, user.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("친구 수락 완료", null));
    }

    // 친구 요청 거절 (내게 온 요청만)
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<String>> rejectFriend(@PathVariable Long id, @AuthenticationPrincipal User user) {
        notificationService.rejectFriendRequest(id, user.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("친구 거절 완료", null));
    }
}
