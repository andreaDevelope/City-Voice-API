package it.cityvoice.api.features.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "campo username obbligatorio")
    @Size(min = 3, max = 14, message = "il campo username deve essere min 3 max 14 caratteri")
    private String username;
    @NotBlank(message = "campo password obbligatorio")
    @Size(min = 4, max = 14, message = "il campo password deve essere min 4 e max 14 caratteri")
    private String password;
}
