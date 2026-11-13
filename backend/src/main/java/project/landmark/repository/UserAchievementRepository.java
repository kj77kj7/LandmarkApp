package project.landmark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.landmark.entity.UserAchievement;
import project.landmark.entity.User;
import java.util.List;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUser(User user);
    boolean existsByUserAndAchievement_Id(User user, Long achievementId);
}
