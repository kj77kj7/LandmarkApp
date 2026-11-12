package project.landmark.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.landmark.dto.*;
import project.landmark.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse resp = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("회원가입이 완료되었습니다.", resp));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("로그인 성공 (JWT 발급됨)", token));
    }

    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@RequestParam String username) {
        boolean isAvailable = authService.isUsernameAvailable(username);
        String msg = isAvailable ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다.";
        return ResponseEntity.ok(ApiResponse.ok(msg, isAvailable));
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestParam String nickname) {
        boolean isAvailable = authService.isNicknameAvailable(nickname);
        String msg = isAvailable ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.";
        return ResponseEntity.ok(ApiResponse.ok(msg, isAvailable));
    }

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        boolean isAvailable = authService.isEmailAvailable(email);
        String msg = isAvailable ? "사용 가능한 이메일입니다." : "이미 가입된 이메일입니다.";
        return ResponseEntity.ok(ApiResponse.ok(msg, isAvailable));
    }

    // 아이디 찾기는 이메일 인증(/api/email-auth/send → /api/email-auth/verify)으로만 할 수 있습니다.

    // 비밀번호 재설정 1단계: 아이디+이메일 확인 후 이메일로 인증번호 발송
    @PostMapping("/check-reset-info")
    public ResponseEntity<ApiResponse<Void>> checkResetInfo(@Valid @RequestBody ResetCodeRequest request) {
        authService.sendResetCode(request.getUsername(), request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("인증번호를 이메일로 보냈습니다.", null));
    }

    // 비밀번호 재설정 2단계: 인증번호 + 새 비밀번호
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getUsername(), request.getCode(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok("비밀번호 재설정 성공", null));
    }
}
