package project.landmark;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import project.landmark.entity.User;
import project.landmark.repository.LandmarkRepository;
import project.landmark.repository.StepRecordRepository;
import project.landmark.repository.UserRepository;
import project.landmark.service.StepService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** H2 메모리 DB로 앱 전체를 띄워서 확인하는 테스트 (MySQL 필요 없음) */
@SpringBootTest
@AutoConfigureMockMvc
class LandmarkApplicationTests {

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepository;
    @Autowired LandmarkRepository landmarkRepository;
    @Autowired StepRecordRepository stepRecordRepository;
    @Autowired StepService stepService;

    @Test
    void contextLoads_그리고_랜드마크_기본_데이터가_들어간다() {
        assertThat(landmarkRepository.findAllByOrderByRequiredStepsAsc())
                .extracting("name")
                .startsWith("해운대", "오사카성");
    }

    @Test
    @Transactional
    void 걸음_수는_한_번만_더해진다() {
        User user = userRepository.save(User.builder()
                .username("walker").password("x").email("walker@test.com").nickname("워커").build());

        stepService.addSteps("walker", 1000);
        stepService.addSteps("walker", 500);

        User saved = userRepository.getByUsername("walker");
        assertThat(saved.getTotalSteps()).isEqualTo(1500L);
        assertThat(stepRecordRepository.findByUserAndDate(user, LocalDate.now()).orElseThrow().getSteps())
                .isEqualTo(1500L);
    }

    @Test
    void 토큰_없이_로그인_필요한_API를_부르면_401() throws Exception {
        mvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void 공개_API는_토큰_없이_된다() throws Exception {
        mvc.perform(get("/api/landmarks")).andExpect(status().isOk());
    }
}
