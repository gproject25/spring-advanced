package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.example.expert.common.JwtFilter;
import org.example.expert.common.JwtUtil;
import org.example.expert.common.LogResponse;
import org.example.expert.common.exception.ErrorResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final JwtUtil jwtUtil;
    private final ErrorResponse errorResponse;
	private final LogResponse logResponse;

    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilter() {
        FilterRegistrationBean<JwtFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new JwtFilter(jwtUtil, errorResponse, logResponse));
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }
}
