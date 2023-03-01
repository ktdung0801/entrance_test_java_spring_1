package com.management.user.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthenticationRequest {

    @NotEmpty
    @Email(message = "Email must be in correct email format")
    private String email;

    @NotEmpty
    @Size(min = 8, max = 20, message = "Password must be between 8-20 characters")
    private String password;
}
