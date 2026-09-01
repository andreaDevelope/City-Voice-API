package it.cityvoice.api.features.auth.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RecoveryRequest {
    @NotBlank(message = "campo username obbligatorio")
    private String username;
    @NotBlank(message = "campo recovery-key obbligatorio")
    private String recoveryKey;
    @NotBlank(message = "campo nuova password obbligatorio")
    @Size(min = 6, max = 12, message = "il campo password deve essere min 6 e max 12 caratteri")
    private String newPassword;
}
