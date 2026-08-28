package it.cityvoice.api.features.auth.dto;
import lombok.Data;

@Data
public class RecoveryRequest {
    private String username;
    private String recoveryKey;
    private String newPassword;
}
