package project.landmark.dto;

import org.junit.jupiter.api.Test;
import project.landmark.entity.Landmark;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LandmarkPositionTest {

    private static Landmark lm(String name, long steps) {
        return Landmark.builder().name(name).requiredSteps(steps).build();
    }

    private final List<Landmark> landmarks = List.of(
            lm("해운대", 0), lm("오사카성", 50_000), lm("에펠탑", 100_000));

    @Test
    void 랜드마크가_없으면_시작점() {
        LandmarkPosition p = LandmarkPosition.of(List.of(), 1234);
        assertThat(p.currentName()).isEqualTo("시작점");
        assertThat(p.next()).isNull();
    }

    @Test
    void 통과한_마지막_랜드마크가_현재_위치() {
        LandmarkPosition p = LandmarkPosition.of(landmarks, 60_000);
        assertThat(p.currentName()).isEqualTo("오사카성");
        assertThat(p.next().getName()).isEqualTo("에펠탑");
    }

    @Test
    void 정확히_도달하면_통과한_것으로_본다() {
        LandmarkPosition p = LandmarkPosition.of(landmarks, 50_000);
        assertThat(p.currentName()).isEqualTo("오사카성");
    }

    @Test
    void 첫_랜드마크도_못_갔으면_입구() {
        List<Landmark> noZero = List.of(lm("오사카성", 50_000), lm("에펠탑", 100_000));
        LandmarkPosition p = LandmarkPosition.of(noZero, 10);
        assertThat(p.currentName()).isEqualTo("오사카성 입구");
        assertThat(p.next().getName()).isEqualTo("오사카성");
    }

    @Test
    void 모두_통과하면_다음_목표는_마지막_랜드마크() {
        LandmarkPosition p = LandmarkPosition.of(landmarks, 999_999);
        assertThat(p.currentName()).isEqualTo("에펠탑");
        assertThat(p.next().getName()).isEqualTo("에펠탑");
    }
}
