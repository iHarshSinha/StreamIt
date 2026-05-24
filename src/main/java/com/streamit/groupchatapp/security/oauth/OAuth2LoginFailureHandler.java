package com.streamit.groupchatapp.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        // Ignore already-used OAuth callback requests
        if (exception instanceof OAuth2AuthenticationException oauthEx) {

            if ("authorization_request_not_found"
                    .equals(oauthEx.getError().getErrorCode())) {

                response.sendRedirect("http://localhost:5173/login");
                return;
            }
        }

        // Real errors
        exception.printStackTrace();

        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "OAuth2 authentication failed"
        );
    }
}