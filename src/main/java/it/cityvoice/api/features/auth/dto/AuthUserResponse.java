package it.cityvoice.api.features.auth.dto;

import it.cityvoice.api.features.auth.enums.Role;

import java.util.Set;

public record AuthUserResponse(String username, Set<Role> roles) {}
