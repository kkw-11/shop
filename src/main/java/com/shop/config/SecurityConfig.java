package com.shop.config;

import com.shop.constant.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")// 특정 경로만 CSRF 비활성화
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/members/**", "/item/**", "/images/**", "/thymeleaf/**").permitAll() // 인증 없이 접근 허용
                        .requestMatchers("/h2-console/**","/css/**", "/js/**", "/img/**").permitAll() // H2 콘솔 접근 허용
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/admin/**").hasRole(Role.ADMIN.name())
                        .anyRequest().authenticated() // 나머지는 인증 필요
                )
                .formLogin(form -> form
                        .loginPage("/members/login") // 커스텀 로그인 페이지 URL
                        .defaultSuccessUrl("/") // 로그인 성공 후 이동
                        .usernameParameter("email")
                        .failureUrl("/members/login/error")
                )
                .logout(logout -> logout
                        .logoutUrl("/members/logout")
                        .logoutSuccessUrl("/")

            ).exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint()) // 커스텀 인증 예외 처리
                )
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())); // H2 콘솔 사용 위해
        ;


        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
