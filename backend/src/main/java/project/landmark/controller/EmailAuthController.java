package project.landmark.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.landmark.dto.ApiResponse;
import project.landmark.service.EmailAuthService;

@RestController
@RequestMapping("/api/email-auth")
@RequiredArgsConstructor
public class EmailAuthController {

    private final EmailAuthService emailAuthService;

    // 1단계: 인증번호 전송 요청
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<String>> sendCode(@RequestParam String nickname, @RequestParam String email) {
        emailAuthService.sendVerificationCode(nickname, email);
        return ResponseEntity.ok(ApiResponse.ok("인증번호가 전송되었습니다.", null));
    }

    // 2단계: 인증번호 검증 및 아이디 반환
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyCode(
            @RequestParam String email,
            @RequestParam String code
    ) {
        String username = emailAuthService.verifyCodeAndGetUsername(email, code);
        return ResponseEntity.ok(ApiResponse.ok("인증 성공", username));
    }
}