package it.cityvoice.api.auth;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;

}
