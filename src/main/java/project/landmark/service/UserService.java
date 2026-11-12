package project.landmark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.landmark.entity.Landmark;
import project.landmark.entity.User;
import project.landmark.repository.LandmarkRepository;
import project.landmark.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final LandmarkRepository landmarkRepository;

    // 도달한 랜드마크의 보상 칭호 목록
    @Transactional(readOnly = true)
    public List<String> getAvailableTitles(String username) {
        User user = userRepository.getByUsername(username);
        long mySteps = LandmarkService.stepsOf(user);

        List<String> titles = new ArrayList<>();
        for (Landmark lm : landmarkRepository.findAllByOrderByRequiredStepsAsc()) {
            long required = lm.getRequiredSteps() != null ? lm.getRequiredSteps() : 0L;
            if (mySteps >= required && lm.getRewardTitle() != null) {
                titles.add(lm.getRewardTitle());
            }
        }
        return titles;
    }

    public void updateRepresentativeTitle(String username, String newTitle) {
        User user = userRepository.getByUsername(username);

        if (newTitle != null && !getAvailableTitles(username).contains(newTitle)) {
            throw new IllegalArgumentException("획득하지 못한 칭호입니다.");
        }
        user.setRepresentativeTitle(newTitle);
    }

    public void updateNickname(String username, String newNickname) {
        User user = userRepository.getByUsername(username);

        if (userRepository.existsByNickname(newNickname)) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }
        user.setNickname(newNickname);
    }

    public void updateProfileImage(String username, String imageUrl) {
        userRepository.getByUsername(username).setProfileImageUrl(imageUrl);
    }

    public void updateStatusMessage(String username, String newMessage) {
        userRepository.getByUsername(username).setStatusMessage(newMessage);
    }
}
