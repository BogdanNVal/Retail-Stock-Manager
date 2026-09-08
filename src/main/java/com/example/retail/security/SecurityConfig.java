package com.example.retail.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername(adminUsername)
                        .password(encoder.encode(adminPassword))
                        .roles("ADMIN")
                        .build()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        RequestMatcher apiMatcher = new AntPathRequestMatcher("/api/**");

        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/produse", "/api/produse/*").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/internal/ready").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/css/**", "/js/**").permitAll()
                        .requestMatchers("/produse/**", "/casa-de-marcat/**").authenticated()
                        .anyRequest().permitAll()
                )
                .httpBasic(withDefaults())
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error"))
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                (request, response, authException) ->
                                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED),
                                apiMatcher
                        )
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new NegatedRequestMatcher(apiMatcher)
                        )
                );
        return http.build();
    }
}
