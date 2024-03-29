package com.example.userservice.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JwtTokenValidator {

    public void validateJwtToken(HttpServletRequest request, HttpServletResponse response, boolean isRefreshValidation) {

        String token = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        String refresh = request.getHeader(SecurityConstants.REFRESH_HEADER);

        log.info("Authorization Token: {}", token);

        if (token != null && token.contains(SecurityConstants.JWT_PREFIX)) {
            try {
                token = token.replace(SecurityConstants.JWT_PREFIX, "");

                SecretKey key = Keys.hmacShaKeyFor(
                        SecurityConstants.JWT_KEY.getBytes(StandardCharsets.UTF_8));

                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(isRefreshValidation ? refresh : token)
                        .getBody();

                String username = String.valueOf(claims.get("username"));
                //String authorities = (String) claims.get("authorities");

                Authentication auth = new UsernamePasswordAuthenticationToken(username, null,
                        null);

                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (ExpiredJwtException ex) {
                log.info("Token expired!");

            } catch (Exception e) {
                throw new BadCredentialsException("Invalid Token received!");
            }
        }
    }

    private List<GrantedAuthority> getStudentRoles(String authorities) {
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<>();
        String[] roles = authorities.split(",");
        for (String role : roles) {
            grantedAuthorityList.add(new SimpleGrantedAuthority(role.replaceAll("\\s+", "")));
        }

        return grantedAuthorityList;
    }
}
