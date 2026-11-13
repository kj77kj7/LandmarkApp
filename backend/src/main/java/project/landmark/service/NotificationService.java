package project.landmark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.landmark.dto.NotificationDto;
import project.landmark.entity.Notification;
import project.landmark.entity.NotificationType;
import project.landmark.entity.User;
import project.landmark.repository.NotificationRepository;
import project.landmark.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FriendService friendService;

    // 내 알림 목록 (페이징)
    @Transactional(readOnly = true)
    public Slice<NotificationDto> getMyNotifications(String username, int page, int size) {
        User user = userRepository.getByUsername(username);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(page, size))
                .map(NotificationDto::from);
    }

    // 안 읽은 알림 개수
    @Transactional(readOnly = true)
    public long getUnreadCount(String username) {
        User user = userRepository.getByUsername(username);
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    // 알림 삭제
    public void deleteNotification(Long id, String myUsername) {
        notificationRepository.delete(getMyNotification(id, myUsername));
    }

    // 친구 수락: 친구 추가에 실패하면 예외가 그대로 올라가고 알림도 지워지지 않습니다.
    public void acceptFriendRequest(Long notificationId, String myUsername) {
        Notification noti = getMyFriendRequest(notificationId, myUsername);
        friendService.addFriend(myUsername, noti.getSenderName());
        notificationRepository.delete(noti);
    }

    // 친구 거절
    public void rejectFriendRequest(Long notificationId, String myUsername) {
        Notification noti = getMyFriendRequest(notificationId, myUsername);

        userRepository.findByNickname(noti.getSenderName()).ifPresent(sender -> {
            User me = userRepository.getByUsername(myUsername);
            notificationRepository.save(Notification.of(sender, NotificationType.FRIEND_REJECT,
                    me.getNickname() + "님이 친구 요청을 거절하셨습니다.", null));
        });

        notificationRepository.delete(noti);
    }

    /** 로그인한 사용자에게 온 알림인지 확인하고 돌려줍니다. 다른 사람 알림이면 403. */
    private Notification getMyNotification(Long id, String myUsername) {
        Notification noti = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("알림이 없습니다."));
        if (!noti.getUser().getUsername().equals(myUsername)) {
            throw new AccessDeniedException("본인의 알림만 처리할 수 있습니다.");
        }
        return noti;
    }

    private Notification getMyFriendRequest(Long id, String myUsername) {
        Notification noti = getMyNotification(id, myUsername);
        if (noti.getType() != NotificationType.FRIEND_REQUEST || noti.getSenderName() == null) {
            throw new IllegalArgumentException("친구 요청 알림이 아닙니다.");
        }
        return noti;
    }
}
