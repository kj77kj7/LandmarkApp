package project.landmark.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.landmark.dto.ApiResponse;
import project.landmark.entity.Landmark;
import project.landmark.repository.LandmarkRepository;

import java.util.List;

@RestController
@RequestMapping("/api/landmarks")
@RequiredArgsConstructor
public class LandmarkController {

    private final LandmarkRepository landmarkRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Landmark>>> getAllLandmarks() {
        // 걸음 수 순서대로 가져오기
        List<Landmark> landmarks = landmarkRepository.findAllByOrderByRequiredStepsAsc();
        return ResponseEntity.ok(ApiResponse.ok("랜드마크 목록 조회 성공", landmarks));
    }
}