package it.cityvoice.api.features.auth.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import it.cityvoice.api.features.auth.util.JwtCookieUtils;
import it.cityvoice.api.features.auth.util.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // 1. Estrae il token JWT dal header Authorization
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            username = extractUsername(jwtToken);
        }

        // 2. Se Authorization header non trovato, cerca nel cookie "access_token"
        if (jwtToken == null) {
            jwtToken = JwtCookieUtils.extractTokenFromCookie(request);
            if (jwtToken != null) {
                username = extractUsername(jwtToken);
            }
        }

        // 3. Se comunque non trovato, passa oltre (utente anonimo)
        if (jwtToken == null) {
            chain.doFilter(request, response);
            return;
        }

        // 4. Valida il token e configura l'autenticazione nel contesto di sicurezza
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);
                if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (UsernameNotFoundException e) {
                log.warn("Token valido per un utente inesistente: {}", username);
            }
        }

        chain.doFilter(request, response);
    }

    // null se il token non è utilizzabile: la richiesta prosegue come anonima
    private String extractUsername(String token) {
        try {
            return jwtTokenUtil.getUsernameFromToken(token);
        } catch (ExpiredJwtException e) {
            log.debug("Token scaduto per l'utente {}", e.getClaims().getSubject());
        } catch (SignatureException e) {
            log.warn("Firma del token non valida: possibile manomissione o chiave JWT cambiata");
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token non valido ({})", e.getClass().getSimpleName());
        }
        return null;
    }
}