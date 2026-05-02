package hattabi.youness.ebanking_chatbot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                            You are a helpful digital banking assistant for Digital Bank.
                            You help customers with their banking questions and account operations.
                            Always be professional, concise, and accurate.
                            If you don't know something, say so clearly.
                            When displaying amounts, always include MAD (Morroccan Dirham) as the currency.
                        """)
                .build();
    }

    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
