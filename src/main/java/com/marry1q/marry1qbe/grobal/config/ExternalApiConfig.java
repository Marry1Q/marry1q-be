package com.marry1q.marry1qbe.grobal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "external-api")
@Getter
@Setter
public class ExternalApiConfig {
    
    private AuthBackend authBackend;
    private HanaBank hanaBank;
    private GeminiAi geminiAi;
    
    @Getter
    @Setter
    public static class AuthBackend {
        private String url;
        private String clientId;
        private String clientSecret;
        private Map<String, String> endpoints;
    }
    
    @Getter
    @Setter
    public static class HanaBank {
        private String url;
        private Integer timeout;
        private Map<String, String> endpoints;
    }
    
    @Getter
    @Setter
    public static class GeminiAi {
        private String url;
        private String apiKey;
    }
}
