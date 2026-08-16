package it.cityvoice.api.auth;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserService appUserService;

    private final JwtTokenUtil jwtTokenUtil;


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        appUserService.registerUser(
                registerRequest,
                Set.of(Role.ROLE_USER) // Assegna il ruolo di default

        );
        return ResponseEntity.ok("Registrazione avvenuta con " +
                "successo");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        String token = appUserService.authenticateUser(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, JwtCookieUtils.createAccessTokenCookie(token).toString())
                .body(new AuthResponse(token));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails user) {

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        AppUser appUser = appUserService.findByUsername(user.getUsername()).orElseThrow(() -> new EntityNotFoundException("User not found"));
        return ResponseEntity.ok(new AuthUserResponse(
                appUser.getUsername(),
                appUser.getRoles()
        ));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@AuthenticationPrincipal UserDetails user, HttpServletRequest request) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        String existingToken = JwtCookieUtils.extractTokenFromCookie(request);
        if (existingToken == null) {
            return ResponseEntity.status(401).build();
        }

        String newToken = jwtTokenUtil.generateToken(user);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, JwtCookieUtils.createAccessTokenCookie(newToken).toString())
                .body(new AuthResponse(newToken));
    }

}
