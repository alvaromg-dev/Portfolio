package com.alvaromg.portfolio.infrastructure.in.http.security;

import com.alvaromg.portfolio.application.roles.constants.RolesConstants;
import com.alvaromg.portfolio.infrastructure.in.http.constants.EndpointsConstants;
import com.alvaromg.portfolio.infrastructure.out.jpa.entities.UserEntity;
import com.alvaromg.portfolio.infrastructure.out.jpa.repository.jpaRepository.UserJpaRepository;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@Slf4j
public class AuthorizationSecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenAuthenticationFilter tokenAuthenticationFilter) throws Exception {
        http.cors(Customizer.withDefaults());
        http.csrf(AbstractHttpConfigurer::disable);
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(EndpointsConstants.getPublicPaths()).permitAll()
            .requestMatchers(
                EndpointsConstants.PORTFOLIO_SAVE,
                EndpointsConstants.TELEMETRY_PAGE,
                EndpointsConstants.TELEMETRY_DELETE
            ).hasAuthority(RolesConstants.CV_EDITOR)
            .requestMatchers(
                EndpointsConstants.USERS_PAGE,
                EndpointsConstants.USERS_CREATE_PAGE,
                EndpointsConstants.USERS_UPDATE_PAGE,
                EndpointsConstants.USERS_DELETE_PAGE
            ).hasAuthority(RolesConstants.ADMIN)
            .anyRequest().authenticated()
        );
        http.formLogin(form -> form
            .loginPage(EndpointsConstants.LOGIN_PAGE)
            .loginProcessingUrl(EndpointsConstants.LOGIN_PAGE)
            .defaultSuccessUrl("/", false)
            .failureUrl(EndpointsConstants.LOGIN_PAGE + "?error=true")
            .permitAll()
        );
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/?logout=true")
            .permitAll()
        );
        http.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserJpaRepository userRepository) {
        return username -> {
            String normalizedUsername = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
            UserEntity user = userRepository.findByEmailWithRolesIgnoreCase(normalizedUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + normalizedUsername));
            Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getCode()))
                .collect(Collectors.toSet());
            boolean enabled = user.getDeletedAt() == null;
            if (!enabled) {
                log.warn("User {} attempted to authenticate but is disabled/deleted", username);
            }
            return new AuthenticatedUser(user.getId(), user.getEmail(), user.getPassword(), authorities, enabled);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern(CorsConfiguration.ALL); // TODO Permissive for dev; tighten for prod
        configuration.addAllowedHeader(CorsConfiguration.ALL);
        configuration.addAllowedMethod(CorsConfiguration.ALL);
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
