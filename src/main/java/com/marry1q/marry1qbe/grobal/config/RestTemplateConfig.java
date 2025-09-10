package com.marry1q.marry1qbe.grobal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000); // 15초로 증가 (연결 타임아웃)
        factory.setReadTimeout(60000);    // 60초로 증가 (AI API 응답 대기)
        
        return new RestTemplate(factory);
    }
}
