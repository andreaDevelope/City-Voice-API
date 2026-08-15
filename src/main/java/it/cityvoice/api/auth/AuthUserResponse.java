package it.cityvoice.api.auth;

import java.util.Set;

public record AuthUserResponse(String username, Set<Role> roles) {}