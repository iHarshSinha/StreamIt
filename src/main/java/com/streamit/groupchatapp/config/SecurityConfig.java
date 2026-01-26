package com.streamit.groupchatapp.config;

import com.streamit.groupchatapp.security.jwt.JwtAuthenticationFilter;
import com.streamit.groupchatapp.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.streamit.groupchatapp.security.oauth.OAuth2LoginFailureHandler;
import com.streamit.groupchatapp.security.oauth.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {

        http
                .cors(cors->{})
                // We are building a stateless REST API
                .csrf(AbstractHttpConfigurer::disable)

                // Disable sessions completely
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Disable default login mechanisms
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth ->
                        oauth
                                .authorizationEndpoint(authEndpoint ->
                                        authEndpoint
                                                .authorizationRequestRepository(
                                                        httpCookieOAuth2AuthorizationRequestRepository
                                                )
                                )
                                .redirectionEndpoint(redirection ->
                                        redirection.baseUri("/login/oauth2/code/*")
                                )
                                .successHandler(oAuth2LoginSuccessHandler)
                                .failureHandler(oAuth2LoginFailureHandler)
                )
                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/public",
                                "/oauth2/**",
                                "/login/**",
                                "/auth/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )



                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }
}