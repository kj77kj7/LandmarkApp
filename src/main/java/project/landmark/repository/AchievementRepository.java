package project.landmark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.landmark.entity.Achievement;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
}
