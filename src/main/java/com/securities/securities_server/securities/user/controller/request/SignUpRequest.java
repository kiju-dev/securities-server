package com.securities.securities_server.securities.user.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(

        @NotBlank(message = "이름은 필수입니다.")
        @Size(min = 2, max = 10, message = "이름은 2글자 이상 10글자 이하여야 합니다.")
        String name,

        @Email
        @NotBlank(message = "이메일은 필수입니다.")
        @Size(min = 10, max = 100, message = "이메일은 10글자 이상 100글자 이하여야 합니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 32, message = "비밀번호는 8글자 이상 32글자 이하여야 합니다.")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$_])(?=.*\\d).{8,32}$",
                message = "비밀번호는 최소 1개 이상의 대소문자, 특수문자, 숫자를 포함해야 합니다."
        )
        String password
) {
}
