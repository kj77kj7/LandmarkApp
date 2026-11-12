package project.landmark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.landmark.dto.LoginRequest;
import project.landmark.dto.RegisterRequest;
import project.landmark.dto.UserResponse;
import project.landmark.entity.User;
import project.landmark.repository.UserRepository;
import project.landmark.security.JwtTokenProvider;
import project.landmark.security.VerificationCodeStore;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String RESET_KEY_PREFIX = "reset-password:";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final VerificationCodeStore codeStore;

    // 회원가입
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자명입니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        User saved = userRepository.save(User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .nickname(request.getNickname())
                .totalSteps(0L)
                .build());

        return UserResponse.from(saved);
    }

    // 로그인
    @Transactional(readOnly = true)
    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return jwtTokenProvider.createToken(user.getId(), user.getUsername());
    }

    // 중복 확인
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    // 비밀번호 재설정 1단계: 아이디와 이메일이 맞으면 그 이메일로 인증번호 발송
    @Transactional(readOnly = true)
    public void sendResetCode(String username, String email) {
        User user = userRepository.findByUsername(username)
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .orElseThrow(() -> new IllegalArgumentException("아이디와 이메일이 일치하는 회원이 없습니다."));

        String code = codeStore.issue(RESET_KEY_PREFIX + username);
        emailService.sendEmail(user.getEmail(), "[Landmark] 비밀번호 재설정 인증번호",
                user.getNickname() + "님, 비밀번호 재설정 인증번호는 [" + code + "] 입니다. 5분 안에 입력해 주세요.");
    }

    // 비밀번호 재설정 2단계: 인증번호가 맞을 때만 변경
    @Transactional
    public void resetPassword(String username, String code, String newPassword) {
        codeStore.verify(RESET_KEY_PREFIX + username, code);

        User user = userRepository.getByUsername(username);
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("이전과 같은 비밀번호는 사용할 수 없습니다.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
    }
}
