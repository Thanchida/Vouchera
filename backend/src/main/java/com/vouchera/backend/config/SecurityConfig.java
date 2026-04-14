package com.vouchera.backend.config;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.vouchera.backend.service.UserService;


@Configuration
public class SecurityConfig {

    @Value("${cors.allowed-origins}")
    private String corsAllowedOrigins;

    private final UserService userService;

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoderConfig passwordEncoderConfig) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoderConfig.passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public HttpSessionSecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookiePath("/");
        csrfTokenRepository.setHeaderName("X-XSRF-TOKEN");

        http
          .cors(Customizer.withDefaults())
          .csrf(csrf -> csrf
              .csrfTokenRepository(csrfTokenRepository)
              .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
              .ignoringRequestMatchers("/api/auth/login", "/api/auth/logout", "/api/users/register", "/api/auth/me")
          )
          .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
          )
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/api/auth/login", "/api/auth/logout", "/api/auth/csrf", "/api/users/register", "/api/auth/me").permitAll()

              .requestMatchers(HttpMethod.GET,
                  "/api/companies",
                  "/api/companies/*",
                  "/api/companies/*/campaigns",
                  "/api/campaigns",
                  "/api/campaigns/*",
                  "/api/voucher-types/**"
              ).permitAll()

              .requestMatchers(HttpMethod.GET, "/api/redemptions/users/**").authenticated()
              .requestMatchers(HttpMethod.POST, "/api/redemptions/redeem").authenticated()

              .requestMatchers(HttpMethod.PATCH, "/api/users/*/status").hasRole("ADMIN")
              .requestMatchers("/api/users/**").hasRole("ADMIN")

              .requestMatchers(HttpMethod.POST, "/api/companies").hasRole("ADMIN")
              .requestMatchers(HttpMethod.PUT, "/api/companies/**").hasRole("ADMIN")
              .requestMatchers(HttpMethod.PATCH, "/api/companies/**").hasRole("ADMIN")

              .requestMatchers(HttpMethod.POST,
                "/api/campaigns",
                "/api/campaigns/*/pause",
                "/api/campaigns/*/resume",
                "/api/campaigns/*/end"
              ).hasAnyRole("ADMIN", "MARKETING")

              .requestMatchers(HttpMethod.PUT, "/api/campaigns/*")
              .hasAnyRole("ADMIN", "MARKETING")

              .requestMatchers(HttpMethod.DELETE, "/api/campaigns/*").hasAnyRole("ADMIN", "MARKETING")

              .requestMatchers(HttpMethod.POST,
                "/api/voucher-types/campaigns/*",
                "/api/voucher-types/*/increase-quota"
              ).hasAnyRole("ADMIN", "MARKETING")

              .anyRequest().authenticated()
          );
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        List<String> origins = List.of(corsAllowedOrigins.split(","));
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
        config.setExposedHeaders(List.of("X-XSRF-TOKEN"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    private static final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

        private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
        private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

        @Override
        public void handle(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            Supplier<CsrfToken> csrfToken
        ) {
            this.xor.handle(request, response, csrfToken);
            csrfToken.get();
        }

        @Override
        public String resolveCsrfTokenValue(jakarta.servlet.http.HttpServletRequest request, CsrfToken csrfToken) {
            String headerValue = request.getHeader(csrfToken.getHeaderName());
            return (StringUtils.hasText(headerValue) ? this.plain : this.xor)
                .resolveCsrfTokenValue(request, csrfToken);
        }
    }
}