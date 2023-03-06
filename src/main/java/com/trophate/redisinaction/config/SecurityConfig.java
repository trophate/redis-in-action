package com.trophate.redisinaction.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trophate.redisinaction.common.Result;
import com.trophate.redisinaction.filter.AuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Configuration
public class SecurityConfig {

    private final RedisTemplate<Object, Object> redisTemplate;
    private final AuthFilter authFilter;

    @Autowired
    public SecurityConfig(RedisTemplate<Object, Object> redisTemplate, AuthFilter authFilter) {
        this.redisTemplate = redisTemplate;
        this.authFilter = authFilter;
    }

    @Bean
    public SecurityFilterChain web(HttpSecurity http) throws Exception {
        http
                .authorizeRequests().mvcMatchers("/login").permitAll().anyRequest().authenticated()
                .and().formLogin().successHandler(new AuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
                        String username = ((User) authentication.getPrincipal()).getUsername();
                        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(5);
                        String token = encoder.encode(username + System.currentTimeMillis());
                        redisTemplate.opsForValue().set(token, username, 30);
                        redisTemplate.opsForHash().put(username, "authentication", authentication);
                        redisTemplate.expire(token, 30, TimeUnit.MINUTES);
                        redisTemplate.expire(username, 30, TimeUnit.MINUTES);
                        response.setContentType("application/json;charset=UTF-8");
                        var objectMapper = new ObjectMapper();
                        response.getWriter().println(objectMapper.writeValueAsString(Result.success().setData(token)));
                    }
                }).failureHandler(new AuthenticationFailureHandler() {
                    @Override
                    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
                        response.setContentType("application/json;charset=UTF-8");
                        var objectMapper = new ObjectMapper();
                        response.getWriter().println(objectMapper.writeValueAsString(Result.fail().setData("用户名或密码错误")));
                    }
                })
                .and().logout()
                .and().exceptionHandling().authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
                        response.setContentType("application/json;charset=UTF-8");
                        var objectMapper = new ObjectMapper();
                        response.getWriter().println(objectMapper.writeValueAsString(Result.fail().setData("未登录")));
                    }
                }).accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
                        response.setContentType("application/json;charset=UTF-8");
                        var objectMapper = new ObjectMapper();
                        response.getWriter().println(objectMapper.writeValueAsString(Result.fail().setData("无权限")));
                    }
                })
                .and().addFilterAfter(authFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement().disable()
                .securityContext().disable()
                .csrf().disable()
                .cors().disable();
        return http.build();
    }
}
