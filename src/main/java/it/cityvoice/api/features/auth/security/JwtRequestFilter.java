package it.cityvoice.api.features.auth.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import it.cityvoice.api.features.auth.util.JwtCookieUtils;
import it.cityvoice.api.features.auth.util.JwtTokenUtil;

import java.io.IOException;

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
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
            } catch (ExpiredJwtException e) {
            } catch (SignatureException e) {
            }
        }

        // 2. Se Authorization header non trovato, cerca nel cookie "access_token"
        if (jwtToken == null) {
            jwtToken = JwtCookieUtils.extractTokenFromCookie(request);
            if (jwtToken != null) {
                try {
                    username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                } catch (IllegalArgumentException e) {
                } catch (ExpiredJwtException e) {
                } catch (SignatureException e) {
                }
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
           }catch (UsernameNotFoundException ignored) {}
        }

        chain.doFilter(request, response);
    }
}
