package com.marry1q.marry1qbe.grobal.jwt;

import com.marry1q.marry1qbe.grobal.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        log.debug("JWT Filter processing request: {} {}", request.getMethod(), requestPath);
        
        try {
            String jwt = getJwtFromRequest(request);
            log.debug("Extracted JWT token: {}", jwt != null ? "TOKEN_PRESENT" : "NO_TOKEN");

            if (StringUtils.hasText(jwt)) {
                boolean isValid = jwtTokenProvider.validateToken(jwt);
                log.debug("JWT token validation result: {}", isValid);
                
                if (isValid) {
                    String userSeqNo = jwtTokenProvider.getUserSeqNoFromToken(jwt);
                    log.debug("UserSeqNo from JWT: {}", userSeqNo);
                    
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(userSeqNo);
                    log.debug("Loaded UserDetails: {}", userDetails.getUsername());
                    
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Authentication set in SecurityContext for user: {}", userSeqNo);
                } else {
                    log.warn("JWT token validation failed for request: {}", requestPath);
                }
            } else {
                log.debug("No JWT token found in request: {}", requestPath);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context for request: {}", requestPath, ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
