package hattabi.youness.ebanking_chatbot.bot;

import org.springframework.stereotype.Component;

import hattabi.youness.ebanking_chatbot.rag.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class IntentRouter {
    private final RagService ragService;

    public enum Intent {
        GREETING, // hello, hi, how are you
        FAQ,
        LIST_CUSTOMERS,
        SEARCH_CUSTOMER,
        LIST_ACCOUNTS,
        GET_ACCOUNT,
        GET_HISTORY,
        DEBIT,
        CREDIT,
        TRANSFER,
        CLEAR_HISTORY,
        UNKNOWN
    }

    public Intent classify(String message) {
        String prompt = """
                Classify this user message into exactly one of these intents.
                Reply with ONLY the intent name, nothing else.

                Intents:
                - GREETING: greetings or small talk (hello, hi, how are you, thanks)
                - FAQ: general banking questions about policies, fees, account types, security
                - LIST_CUSTOMERS: user wants to see all customers
                - SEARCH_CUSTOMER: user wants to find a specific customer by name
                - LIST_ACCOUNTS: user wants to see all bank accounts
                - GET_ACCOUNT: user wants details of a specific account by ID
                - GET_HISTORY: user wants transaction history of an account
                - DEBIT: user wants to withdraw or debit money from an account
                - CREDIT: user wants to deposit or credit money to an account
                - TRANSFER: user wants to transfer money between accounts
                - CLEAR_HISTORY: user wants to reset or clear the conversation
                - UNKNOWN: anything else

                User message: "%s"

                Intent:""".formatted(message);

        try {
            String result = ragService.askDirect(prompt)
                    .trim()
                    .toUpperCase()
                    .replaceAll("[^A-Z_]", ""); // strip any extra chars

            log.debug("Classified '{}' as {}", message, result);
            return Intent.valueOf(result);
        } catch (Exception e) {
            log.warn("Could not classify intent for: '{}' — defaulting to UNKNOWN", message);
            return Intent.UNKNOWN;
        }
    }

    public String extractEntity(String message, String entityType) {
        String prompt = """
                Extract the %s from this message.
                Reply with ONLY the extracted value, nothing else.
                If not found, reply with: NOT_FOUND

                Message: "%s"

                %s:""".formatted(entityType, message, entityType);

        try {
            String result = ragService.askDirect(prompt).trim();
            return result.equals("NOT_FOUND") ? null : result;
        } catch (Exception e) {
            log.warn("Entity extraction failed: {}", e.getMessage());
            return null;
        }
    }
}
