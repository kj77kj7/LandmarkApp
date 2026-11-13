package project.landmark.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {


    @NotBlank
    @Size(min = 3, max = 50)
    private String username;


    @NotBlank
    @Size(min = 8, max = 100)
    private String password; // 원문 비밀번호 (전송 시 반드시 HTTPS 권장)


    @NotBlank
    @Email
    private String email;


    @NotBlank
    @Size(min = 2, max = 30)
    private String nickname;
}
