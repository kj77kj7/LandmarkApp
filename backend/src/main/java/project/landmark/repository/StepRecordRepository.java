package project.landmark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import project.landmark.entity.StepRecord;
import project.landmark.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StepRecordRepository extends JpaRepository<StepRecord, Long> {

    Optional<StepRecord> findByUserAndDate(User user, LocalDate date);

    // 유저의 모든 걸음 기록을 날짜순으로 조회 (연속 달성 체크용)
    List<StepRecord> findAllByUserOrderByDateAsc(User user);

    // 유저의 모든 걸음 기록 조회 (하루 N보 달성 체크용)
    List<StepRecord> findAllByUser(User user);

    @Query("SELECT sr.user, SUM(sr.steps) " +
            "FROM StepRecord sr " +
            "WHERE sr.date BETWEEN :start AND :end " +
            "GROUP BY sr.user " +
            "ORDER BY SUM(sr.steps) DESC")
    List<Object[]> sumStepsByUserBetweenDates(LocalDate start, LocalDate end);

    @Query("SELECT sr.user, SUM(sr.steps) " +
            "FROM StepRecord sr " +
            "WHERE sr.date BETWEEN :start AND :end " +
            "AND sr.user.district = :district " +
            "GROUP BY sr.user " +
            "ORDER BY SUM(sr.steps) DESC")
    List<Object[]> sumStepsByDistrictBetweenDates(LocalDate start, LocalDate end, String district);
}