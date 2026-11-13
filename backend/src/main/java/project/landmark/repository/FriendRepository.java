package project.landmark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.landmark.entity.Friend;
import project.landmark.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    // 친구 랭킹용: 내 친구 User 목록
    @Query("SELECT f.friend FROM Friend f WHERE f.user = :user")
    List<User> findFriendsByUser(@Param("user") User user);

    // 내 친구 관계 목록
    List<Friend> findByUser(User user);

    // 특정 친구 관계 찾기
    Optional<Friend> findByUserAndFriend(User user, User friend);
}
