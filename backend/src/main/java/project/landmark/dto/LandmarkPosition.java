package project.landmark.dto;

import project.landmark.entity.Landmark;

import java.util.List;

/**
 * 누적 걸음 수로 계산한 현재 위치와 다음 목표 랜드마크.
 * 홈 화면, 내 정보, 친구 목록, 랭킹이 모두 이 계산 하나를 씁니다.
 *
 * @param currentName 마지막으로 통과한 랜드마크 이름 (아직 없으면 "첫 랜드마크 입구", 데이터가 없으면 "시작점")
 * @param next        다음 목표 랜드마크 (모두 통과했으면 마지막 랜드마크, 데이터가 없으면 null)
 */
public record LandmarkPosition(String currentName, Landmark next) {

    /** @param sortedAsc requiredSteps 오름차순으로 정렬된 랜드마크 목록 */
    public static LandmarkPosition of(List<Landmark> sortedAsc, long steps) {
        if (sortedAsc.isEmpty()) {
            return new LandmarkPosition("시작점", null);
        }

        Landmark current = null;
        Landmark next = null;
        for (Landmark lm : sortedAsc) {
            long required = lm.getRequiredSteps() != null ? lm.getRequiredSteps() : 0L;
            if (steps >= required) {
                current = lm;
            } else {
                next = lm;
                break;
            }
        }
        if (next == null) {
            next = sortedAsc.get(sortedAsc.size() - 1);
        }

        String currentName = current != null ? current.getName() : sortedAsc.get(0).getName() + " 입구";
        return new LandmarkPosition(currentName, next);
    }
}
