package project.landmark.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import project.landmark.entity.Notification;
import project.landmark.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 페이징(무한 스크롤)을 위해 Slice 사용
    Slice<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // 안 읽은 알림 개수 조회 (isRead가 false인 것만 카운트)
    // 실제로는 삭제 기능이 "읽음 처리" 역할을 겸하므로, 현재 남아있는 알림 중 읽지 않은 것을 셀 수 있습니다.
    // 만약 "삭제"가 아예 DB에서 지우는 것이라면 countByUser(user)를 써야 할 수도 있지만,
    // 요청하신 시나리오(X 누르면 삭제 -> 개수 줄어듦)에 맞춰 '안 읽은 상태'를 기준으로 카운트합니다.
    long countByUserAndIsReadFalse(User user);
}