package project.landmark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.landmark.dto.FriendDto;
import project.landmark.entity.*;
import project.landmark.repository.FriendRepository;
import project.landmark.repository.NotificationRepository;
import project.landmark.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final LandmarkService landmarkService;
    private final NotificationRepository notificationRepository;

    // 친구 목록 (ACCEPTED 상태만)
    @Transactional(readOnly = true)
    public List<FriendDto> getMyFriends(String username) {
        User user = userRepository.getByUsername(username);

        return friendRepository.findByUser(user).stream()
                .filter(f -> f.getStatus() == FriendStatus.ACCEPTED)
                .map(f -> {
                    User friendUser = f.getFriend();
                    return FriendDto.builder()
                            .id(f.getId())
                            .friendNickname(friendUser.getNickname())
                            .status(f.getStatus().name())
                            .currentLandmark(landmarkService.getCurrentLandmarkName(friendUser))
                            .totalSteps(friendUser.getTotalSteps())
                            .profileImageUrl(friendUser.getProfileImageUrl())
                            .representativeTitle(friendUser.getRepresentativeTitle())
                            .statusMessage(friendUser.getStatusMessage())
                            .build();
                })
                .toList();
    }

    // 친구 요청 보내기: 상대에게 '친구 요청' 알림만 만들고, 수락하면 addFriend가 호출됩니다.
    public void requestFriend(String myUsername, String friendNickname) {
        User me = userRepository.getByUsername(myUsername);
        User friendUser = userRepository.findByNickname(friendNickname)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        if (me.getId().equals(friendUser.getId())) {
            throw new IllegalArgumentException("자기 자신에게 요청할 수 없습니다.");
        }

        boolean isAlreadyFriend = friendRepository.findByUserAndFriend(me, friendUser)
                .map(f -> f.getStatus() == FriendStatus.ACCEPTED)
                .orElse(false);
        if (isAlreadyFriend) {
            throw new IllegalArgumentException("이미 친구입니다.");
        }

        notificationRepository.save(Notification.of(friendUser, NotificationType.FRIEND_REQUEST,
                me.getNickname() + "님이 친구 요청을 보냈습니다.", me));
    }

    // 친구 수락: 양방향으로 저장하고 요청자에게 수락 알림
    public void addFriend(String myUsername, String friendNickname) {
        User me = userRepository.getByUsername(myUsername);
        User friendUser = userRepository.findByNickname(friendNickname)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 닉네임입니다."));

        saveAccepted(me, friendUser);
        saveAccepted(friendUser, me);

        notificationRepository.save(Notification.of(friendUser, NotificationType.FRIEND_ACCEPT,
                me.getNickname() + "님이 친구 요청을 수락하셨습니다!", me));
    }

    // 친구 삭제: 양쪽 관계를 지우고 상대에게 알림
    public void deleteFriend(String myUsername, String friendNickname) {
        User me = userRepository.getByUsername(myUsername);
        User friendUser = userRepository.findByNickname(friendNickname)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 친구입니다."));

        friendRepository.findByUserAndFriend(me, friendUser).ifPresent(friendRepository::delete);
        friendRepository.findByUserAndFriend(friendUser, me).ifPresent(friendRepository::delete);

        notificationRepository.save(Notification.of(friendUser, NotificationType.FRIEND_REJECT,
                me.getNickname() + "님과 절교하셨습니다.", me));
    }

    private void saveAccepted(User user, User friend) {
        Friend friendship = friendRepository.findByUserAndFriend(user, friend)
                .orElse(Friend.builder().user(user).friend(friend).build());
        friendship.setStatus(FriendStatus.ACCEPTED);
        friendRepository.save(friendship);
    }
}
