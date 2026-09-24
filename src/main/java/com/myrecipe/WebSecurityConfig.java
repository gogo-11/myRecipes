package com.myrecipe;

import java.util.Arrays;
import java.util.Collections;

import com.myrecipe.security.JwtAuthenticationFilter;
import com.myrecipe.security.JwtProperties;
import com.myrecipe.security.RestAccessDeniedHandler;
import com.myrecipe.security.RestAuthenticationEntryPoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class WebSecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    public WebSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                             RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                             RestAccessDeniedHandler restAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http.antMatcher("/api/**")
                .cors().configurationSource(apiCorsConfigurationSource())
                .and()
                .csrf().disable()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                    .authenticationEntryPoint(restAuthenticationEntryPoint)
                    .accessDeniedHandler(restAccessDeniedHandler)
                .and()
                .authorizeRequests()
                    .antMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                    .antMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                    .antMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                    .antMatchers(HttpMethod.POST, "/api/v1/auth/email-confirmations/confirm").permitAll()
                    .antMatchers(HttpMethod.POST, "/api/v1/auth/email-confirmations/resend").permitAll()
                    .antMatchers(HttpMethod.GET, "/api/v1/recipes").permitAll()
                    .regexMatchers(HttpMethod.GET, "/api/v1/recipes/[0-9]+").permitAll()
                    .regexMatchers(HttpMethod.GET, "/api/v1/recipes/[0-9]+/image").permitAll()
                    .antMatchers(HttpMethod.POST, "/api/v1/recipes").denyAll()
                    .antMatchers(HttpMethod.POST, "/api/v1/recipes/allUserPrivate").denyAll()
                    .antMatchers(HttpMethod.DELETE, "/api/v1/recipes/**").denyAll()
                    .regexMatchers(HttpMethod.POST, "/api/v1/recipes/[0-9]+/nutrition").denyAll()
                    .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public SecurityFilterChain mvcSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                    .antMatchers("/user/**", "/comments/**").denyAll()
                    .anyRequest().permitAll()
                .and()
                .exceptionHandling()
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.FORBIDDEN))
                .and()
                .formLogin()
                    .loginPage("/login_form")
                    .usernameParameter("email")
                    .permitAll()
                .and()
                .logout()
                    .logoutSuccessUrl("/welcome");

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource apiCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
