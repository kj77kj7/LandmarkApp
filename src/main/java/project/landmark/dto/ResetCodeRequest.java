package project.landmark.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 비밀번호 재설정 1단계: 아이디+이메일 확인 후 인증번호 발송 */
@Getter
@Setter
@NoArgsConstructor
public class ResetCodeRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;
}
