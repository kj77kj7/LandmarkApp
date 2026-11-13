package project.landmark.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 비밀번호 재설정 2단계: 이메일로 받은 인증번호 + 새 비밀번호 */
@Getter
@Setter
@NoArgsConstructor
public class ResetPasswordRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String code;

    @NotBlank
    @Size(min = 8, max = 100)
    private String newPassword;
}
