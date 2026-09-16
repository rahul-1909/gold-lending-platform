package com.starfinance.handler;


import com.starfinance.service.JwtService;
import com.starfinance.service.OAuthUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final OAuthUserService oAuthUserService;

    @org.springframework.beans.factory.annotation.Value("${frontend.url:https://starfinance-app.vercel.app}")
    private String frontendUrl;

    public OAuth2AuthenticationSuccessHandler(JwtService jwtService, OAuthUserService oAuthUserService) {
        this.jwtService = jwtService;
        this.oAuthUserService = oAuthUserService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String baseUrl = (frontendUrl != null && !frontendUrl.isBlank()) ? frontendUrl : "https://starfinance-app.vercel.app";
        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            oAuthUserService.createOrGetCustomer(
                    (OAuth2User) authentication.getPrincipal(),
                    oAuth2User.getAttribute("sub") != null ? oAuth2User.getAttribute("sub") : oAuth2User.getAttribute("id"),
                    authentication.getAuthorities().stream().map(Object::toString).collect(Collectors.joining(",")));

            String jwt = jwtService.generateToken(authentication);
            response.sendRedirect(baseUrl + "/login?token=" + jwt);
        } else {
            response.sendRedirect(baseUrl + "/login?error=auth_failed");
        }
    }
}