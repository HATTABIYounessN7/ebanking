package hattabi.youness.ebanking_chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${banking.api.base-url")
    private String bankingApiBaseUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(bankingApiBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
