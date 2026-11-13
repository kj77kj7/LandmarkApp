package project.landmark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.landmark.entity.Landmark;
import java.util.List;

public interface LandmarkRepository extends JpaRepository<Landmark, Long> {
    // 랜드마크를 걸음 수 순서대로 정렬해서 가져오는 메서드
    List<Landmark> findAllByOrderByRequiredStepsAsc();
}