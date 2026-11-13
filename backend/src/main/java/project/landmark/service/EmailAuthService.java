package project.landmark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.landmark.entity.User;
import project.landmark.repository.UserRepository;
import project.landmark.security.VerificationCodeStore;

/** 아이디 찾기: 닉네임+이메일 확인 → 이메일로 인증번호 발송 → 인증번호 확인 후 아이디 반환 */
@Service
@RequiredArgsConstructor
public class EmailAuthService {

    private static final String KEY_PREFIX = "find-id:";

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final VerificationCodeStore codeStore;

    @Transactional(readOnly = true)
    public void sendVerificationCode(String nickname, String email) {
        User user = userRepository.findByNicknameAndEmail(nickname, email)
                .orElseThrow(() -> new IllegalArgumentException("입력하신 정보와 일치하는 회원이 없습니다."));

        String code = codeStore.issue(KEY_PREFIX + email);
        emailService.sendEmail(email, "[Landmark] 아이디 찾기 인증번호",
                user.getNickname() + "님, 인증번호는 [" + code + "] 입니다. 5분 안에 입력해 주세요.");
    }

    @Transactional(readOnly = true)
    public String verifyCodeAndGetUsername(String email, String inputCode) {
        codeStore.verify(KEY_PREFIX + email, inputCode);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."))
                .getUsername();
    }
}
