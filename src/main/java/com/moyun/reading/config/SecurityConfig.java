package com.moyun.reading.config;

import com.moyun.reading.control.service.UserService;
import com.moyun.reading.entity.base.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import java.util.Collections;

/**
 * Spring Security 配置类
 * 
 * @author Moyun Team
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final UserService userService;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public HttpFirewall httpFirewall() {
                StrictHttpFirewall firewall = new StrictHttpFirewall();
                firewall.setAllowUrlEncodedDoubleSlash(true);
                return firewall;
        }

        @Bean
        public UserDetailsService userDetailsService() {
                return username -> {
                        User user = userService.findByUsername(username)
                                        .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

                        if (!user.isAccountAvailable()) {
                                throw new UsernameNotFoundException("账户不可用: " + username);
                        }

                        return new org.springframework.security.core.userdetails.User(
                                        user.getUsername(),
                                        user.getPassword(),
                                        user.getEnabled(),
                                        user.getAccountNonExpired(),
                                        user.getCredentialsNonExpired(),
                                        user.getAccountNonLocked(),
                                        Collections.singletonList(
                                                        new SimpleGrantedAuthority(user.getUserRole().getAuthority())));
                };
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(authz -> authz
                                                // 公开访问的路径 - 主页可访问但内容会根据登录状态显示
                                                .requestMatchers("/", "/home", "/user/register", "/user/login",
                                                                "/user/forgot-password", "/user/reset-password",
                                                                "/user/reset-password-direct",
                                                                "/info/**", "/css/**", "/js/**",
                                                                "/images/**", "/favicon.ico", "/error")
                                                .permitAll()
                                                .requestMatchers("/h2-console/**").permitAll()

                                                // 管理员专用路径
                                                .requestMatchers("/admin/**").hasRole("ADMIN")

                                                // 导师权限路径
                                                .requestMatchers("/teacher/**").hasAnyRole("TEACHER", "ADMIN")

                                                // 学生权限路径
                                                .requestMatchers("/student/**")
                                                .hasAnyRole("STUDENT", "TEACHER", "ADMIN")

                                                // 其他需要认证的路径 (包括搜索、图书、书评等功能)
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/user/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/", true)
                                                .failureUrl("/user/login?error")
                                                .usernameParameter("username")
                                                .passwordParameter("password")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/") // 退出后回到主页
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .sessionManagement(session -> session
                                                .maximumSessions(1)
                                                .maxSessionsPreventsLogin(false)
                                                .expiredUrl("/user/login?expired"))
                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                                .ignoringRequestMatchers("/h2-console/**"))
                                .headers(headers -> headers
                                                .frameOptions().sameOrigin());

                return http.build();
        }
}