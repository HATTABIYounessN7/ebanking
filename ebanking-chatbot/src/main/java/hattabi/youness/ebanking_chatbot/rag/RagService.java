package hattabi.youness.ebanking_chatbot.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {
    private final ChatClient chatClient;
    private final SimpleVectorStore vectorStore;
    private final ConversationMemory conversationMemory;

    public String askWithContext(String userQuestion, Long chatId) {
        log.debug("RAG query: {}", userQuestion);

        try {
            String history = conversationMemory.formatHistoryAsContext(chatId);
            String fullQuestion = history.isEmpty()
                    ? userQuestion
                    : history + "\nCurrentQuestion: " + userQuestion;

            String response = chatClient.prompt()
                    .user(fullQuestion)
                    .advisors(new QuestionAnswerAdvisor(vectorStore,
                            SearchRequest.builder().topK(4).similarityThreshold(.5).build()))
                    .call()
                    .content();

            conversationMemory.addMessage(chatId, "user", userQuestion);
            conversationMemory.addMessage(chatId, "assistant", response);
            log.debug("RAG response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("RAG query failed: {}", e.getMessage());
            return "Could not process question, Try again later";
        }
    }

    public String askWithContextAndData(String userQuestion, String liveData, Long chatId) {
        log.debug("RAG + live data query: {}", userQuestion);

        try {
            String history = conversationMemory.formatHistoryAsContext(chatId);
            String enrichedQuestion = """
                    %s
                    Current question: %s

                    Live banking data:
                    %s
                    """.formatted(history, userQuestion, liveData);

            String response = chatClient.prompt()
                    .user(enrichedQuestion)
                    .advisors(new QuestionAnswerAdvisor(vectorStore,
                            SearchRequest.builder()
                                    .topK(3)
                                    .similarityThreshold(.4)
                                    .build()))
                    .call().content();

            conversationMemory.addMessage(chatId, "user", userQuestion);
            conversationMemory.addMessage(chatId, "assistant", response);
            log.debug("RAG + data response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("RAG + data query failed: {}", e.getMessage());
            return "Could not process question, Try again later";
        }
    }

    public String askDirect(String userMessage) {
        log.debug("Direct GPT-40 query: {}", userMessage);

        try {
            return chatClient.prompt()
                    .user(userMessage)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Direct query failed: {}", e.getMessage());
            return "Could not process question, Try again later";
        }
    }

    public String formatOperationResult(
            String operationType,
            String accountId,
            double amount,
            boolean success,
            String errorMessage) {

        String prompt = success
                ? """
                        Format a friendly success confirmation for this banking operation:
                        - Operation: %s
                        - Account ID: %s
                        - Amount: %.2f MAD
                        Keep it concise, professional, and reassuring.
                        """.formatted(operationType, accountId, amount)
                : """
                        Format a friendly error message for a failed banking operation:
                        - Operation: %s
                        - Account ID: %s
                        - Amount: %.2f MAD
                        - Reason: %s
                        Keep it concise and suggest what the user can do next.
                        """.formatted(operationType, accountId, amount, errorMessage);

        return askDirect(prompt);
    }
}
