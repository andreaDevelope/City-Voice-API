package it.cityvoice.api.features.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "username cant be empty")
    private String username;
    @NotBlank(message = "password cant be empty")
    private String password;
}
