package it.cityvoice.api.features.auth.service;

import it.cityvoice.api.shared.exceptions.BadRequestException;
import it.cityvoice.api.shared.exceptions.ConflictException;
import it.cityvoice.api.shared.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import it.cityvoice.api.features.auth.entity.AppUser;
import it.cityvoice.api.features.auth.repository.AppUserRepository;
import it.cityvoice.api.features.auth.Role;
import it.cityvoice.api.features.auth.dto.RegisterRequest;
import it.cityvoice.api.features.auth.dto.RecoveryRequest;
import it.cityvoice.api.features.auth.util.JwtTokenUtil;

import java.util.Optional;
import java.util.Set;

@Service
public class AppUserService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RecoveryKeyService recoveryKeyService;

    @Autowired
    private RecoveryAttemptLimiter recoveryAttemptLimiter;

    public record RegistrationResult(AppUser user, String recoveryKey) {}

    public RegistrationResult registerUser(RegisterRequest registerRequest, Set<Role> roles) {
        if (appUserRepository.existsByUsername(registerRequest.getUsername())) {
            throw new ConflictException("Username già in uso");
        }

        RecoveryKeyService.GeneratedKey generatedKey = recoveryKeyService.generate();

        AppUser appUser = new AppUser();
        appUser.setUsername(registerRequest.getUsername());
        appUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        appUser.setRoles(roles);
        appUser.setRecoveryKeyHash(generatedKey.hashedKey());

        AppUser savedUser = appUserRepository.save(appUser);
        return new RegistrationResult(savedUser, generatedKey.plainKey());
    }

    public Optional<AppUser> findByUsername(String username) {
        return appUserRepository.findByUsername(username);
    }

    public String authenticateUser(String username, String password)  {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return jwtTokenUtil.generateToken(userDetails);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Username o password non validi", e);
        }
    }


    public AppUser loadUserByUsername(String username)  {
        return appUserRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con username: " + username));
    }

    public void recoverAccount(RecoveryRequest request) {
        if (recoveryAttemptLimiter.isLocked(request.getUsername())) {
            throw new BadRequestException("Troppi tentativi falliti, riprova più tardi");
        }

        AppUser user = appUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Username o chiave di recovery non validi"));

        if (!recoveryKeyService.matches(request.getRecoveryKey(), user.getRecoveryKeyHash())) {
            recoveryAttemptLimiter.recordFailure(request.getUsername());
            throw new BadRequestException("Username o chiave di recovery non validi");
        }

        recoveryAttemptLimiter.recordSuccess(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        appUserRepository.save(user);
    }
}
