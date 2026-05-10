package hattabi.youness.ebanking_chatbot.bot;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import hattabi.youness.ebanking_chatbot.banking.BankingApiClient;
import hattabi.youness.ebanking_chatbot.banking.BankingApiModels;
import hattabi.youness.ebanking_chatbot.rag.ConversationMemory;
import hattabi.youness.ebanking_chatbot.rag.RagService;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TelegramBot implements SpringLongPollingBot, LongPollingUpdateConsumer {
    private final TelegramClient telegramClient;
    private final IntentRouter intentRouter;
    private final RagService ragService;
    private final BankingApiClient bankingApiClient;
    private final ConversationMemory conversationMemory;
    private final String botToken;
    private final String botUsername;

    public TelegramBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            IntentRouter intentRouter,
            RagService ragService,
            BankingApiClient bankingApiClient,
            ConversationMemory conversationMemory) {

        this.botToken = botToken;
        this.botUsername = botUsername;
        this.intentRouter = intentRouter;
        this.ragService = ragService;
        this.bankingApiClient = bankingApiClient;
        this.conversationMemory = conversationMemory;
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(List<Update> updates) {
        updates.forEach(this::handleUpdate);
    }

    private void handleUpdate(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText())
            return;

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText().trim();
        String fname = update.getMessage().getFrom().getFirstName();

        log.info("Message from {} ({}): {}", fname, chatId, text);

        String response = routeMessage(chatId, text, fname);
        sendMessage(chatId, response);
    }

    private String routeMessage(Long chatId, String text, String fname) {
        if (text.startsWith("/")) {
            return handleCommand(chatId, text, fname);
        }

        IntentRouter.Intent intent = intentRouter.classify(text);
        log.debug("Intent for '{}': {}", text, intent);

        return switch (intent) {
            case GREETING -> handleGreeting(fname);
            case FAQ -> handleFaq(chatId, text);
            case LIST_CUSTOMERS -> handleListCustomers(chatId, text);
            case SEARCH_CUSTOMER -> handleSearchCustomer(chatId, text);
            case LIST_ACCOUNTS -> handleListAccounts(chatId, text);
            case GET_ACCOUNT -> handleGetAccount(chatId, text);
            case GET_HISTORY -> handleGetHistory(chatId, text);
            case DEBIT -> handleDebit(chatId, text);
            case CREDIT -> handleCredit(chatId, text);
            case TRANSFER -> handleTransfer(chatId, text);
            case CLEAR_HISTORY -> handleClearHistory(chatId);
            default -> handleUnknown(chatId, text);
        };
    }

    private String handleCommand(Long chatId, String command, String fname) {
        return switch (command.toLowerCase()) {
            case "/start" -> """
                    👋 Welcome to Digital Bank Assistant, %s!

                    I can help you with:
                    📋 /customers — list all customers
                    💳 /accounts — list all accounts
                    ❓ /help — show this menu
                    🗑 /clear — clear conversation history

                    Or just ask me anything in plain language!
                    Example: "What is the balance of account X?"
                    """.formatted(fname);

            case "/customers" -> handleListCustomers(chatId, command);
            case "/accounts" -> handleListAccounts(chatId, command);
            case "/clear" -> handleClearHistory(chatId);
            case "/help" -> """
                    🏦 *Digital Bank Assistant — Help*

                    *Commands:*
                    /start — welcome message
                    /customers — list all customers
                    /accounts — list all accounts
                    /clear — reset conversation
                    /help — show this menu

                    *You can also ask in plain language:*
                    • "Show me customer Hassan's accounts"
                    • "What is the balance of account [ID]?"
                    • "Debit 500 MAD from account [ID]"
                    • "Credit 1000 MAD to account [ID]"
                    • "Transfer 200 MAD from [ID] to [ID]"
                    • "Show history for account [ID]"
                    • "What are the overdraft fees?"
                    """;
            default -> "Unknown command. Type /help to see available commands.";
        };
    }

    private String handleGreeting(String fname) {
        return "👋 Hello %s! I'm your Digital Bank Assistant. How can I help you today? Type /help to see what I can do."
                .formatted(fname);
    }

    private String handleFaq(Long chatId, String question) {
        String answer = ragService.askWithContext(question, chatId);
        return "💬 " + answer;
    }

    private String handleListCustomers(Long chatId, String text) {
        try {
            List<BankingApiModels.Customer> customers = bankingApiClient.getAllCustomers();

            if (customers.isEmpty())
                return "No customers found in the system.";

            String data = bankingApiClient.formatCustomersAsText(customers);
            String summary = ragService.askWithContextAndData(
                    "Summarize this customer list clearly and concisely:",
                    data, chatId);

            return "👥 *Customers (%d):*\n\n".formatted(customers.size())
                    + summary;

        } catch (Exception e) {
            log.error("List customers failed: {}", e.getMessage());
            return "❌ Could not fetch customers. Is the banking API running?";
        }
    }

    private String handleSearchCustomer(Long chatId, String text) {
        try {
            String keyword = intentRouter.extractEntity(text, "customer name");
            if (keyword == null)
                return "Please provide a customer name to search for.";

            List<BankingApiModels.Customer> results = bankingApiClient.searchCustomers(keyword);

            if (results.isEmpty())
                return "No customers found matching: " + keyword;

            String data = bankingApiClient.formatCustomersAsText(results);
            return "🔍 *Search results for '%s':*\n\n%s"
                    .formatted(keyword, data);

        } catch (Exception e) {
            log.error("Search customer failed: {}", e.getMessage());
            return "❌ Search failed. Please try again.";
        }
    }

    private String handleListAccounts(Long chatId, String text) {
        try {
            List<BankingApiModels.BankAccount> accounts = bankingApiClient.getAllAccounts();

            if (accounts.isEmpty())
                return "No accounts found in the system.";

            String data = bankingApiClient.formatAccountsAsText(accounts);
            String summary = ragService.askWithContextAndData(
                    "Summarize this list of bank accounts clearly:",
                    data, chatId);

            return "💳 *Accounts (%d):*\n\n".formatted(accounts.size())
                    + summary;

        } catch (Exception e) {
            log.error("List accounts failed: {}", e.getMessage());
            return "❌ Could not fetch accounts. Is the banking API running?";
        }
    }

    private String handleGetAccount(Long chatId, String text) {
        try {
            String accountId = intentRouter.extractEntity(text, "account ID");
            if (accountId == null)
                return "Please provide an account ID.";

            Optional<BankingApiModels.BankAccount> account = bankingApiClient.getAccount(accountId);

            if (account.isEmpty())
                return "❌ Account not found: " + accountId;

            String data = bankingApiClient
                    .formatAccountsAsText(List.of(account.get()));

            return ragService.askWithContextAndData(
                    "Present these account details clearly to the customer:",
                    data, chatId);

        } catch (Exception e) {
            log.error("Get account failed: {}", e.getMessage());
            return "❌ Could not fetch account details.";
        }
    }

    private String handleGetHistory(Long chatId, String text) {
        try {
            String accountId = intentRouter.extractEntity(text, "account ID");
            if (accountId == null)
                return "Please provide an account ID to see its history.";

            Optional<BankingApiModels.AccountHistory> history = bankingApiClient.getAccountHistory(accountId, 0, 5);

            if (history.isEmpty())
                return "❌ No history found for account: " + accountId;

            String data = bankingApiClient
                    .formatHistoryAsText(history.get());

            return "📜 *Account History:*\n\n" + data;

        } catch (Exception e) {
            log.error("Get history failed: {}", e.getMessage());
            return "❌ Could not fetch account history.";
        }
    }

    private String handleDebit(Long chatId, String text) {
        try {
            String accountId = intentRouter.extractEntity(text, "account ID");
            String amountStr = intentRouter.extractEntity(text, "amount as a number without currency");

            if (accountId == null)
                return "Please provide the account ID for the debit operation.";
            if (amountStr == null)
                return "Please provide the amount to debit.";

            double amount = Double.parseDouble(amountStr);
            String result = bankingApiClient.debit(
                    accountId, amount, "Debit via chatbot");

            return result.equals("success")
                    ? ragService.formatOperationResult(
                            "DEBIT", accountId, amount, true, null)
                    : ragService.formatOperationResult(
                            "DEBIT", accountId, amount, false,
                            result.replace("error:", ""));

        } catch (NumberFormatException e) {
            return "❌ Invalid amount. Please provide a valid number.";
        } catch (Exception e) {
            log.error("Debit failed: {}", e.getMessage());
            return "❌ Debit operation failed: " + e.getMessage();
        }
    }

    private String handleCredit(Long chatId, String text) {
        try {
            String accountId = intentRouter.extractEntity(text, "account ID");
            String amountStr = intentRouter.extractEntity(text, "amount as a number without currency");

            if (accountId == null)
                return "Please provide the account ID for the credit operation.";
            if (amountStr == null)
                return "Please provide the amount to credit.";

            double amount = Double.parseDouble(amountStr);
            String result = bankingApiClient.credit(
                    accountId, amount, "Credit via chatbot");

            return result.equals("success")
                    ? ragService.formatOperationResult(
                            "CREDIT", accountId, amount, true, null)
                    : ragService.formatOperationResult(
                            "CREDIT", accountId, amount, false,
                            result.replace("error:", ""));

        } catch (NumberFormatException e) {
            return "❌ Invalid amount. Please provide a valid number.";
        } catch (Exception e) {
            log.error("Credit failed: {}", e.getMessage());
            return "❌ Credit operation failed: " + e.getMessage();
        }
    }

    private String handleTransfer(Long chatId, String text) {
        try {
            String sourceId = intentRouter.extractEntity(
                    text, "source account ID");
            String destId = intentRouter.extractEntity(
                    text, "destination account ID");
            String amountStr = intentRouter.extractEntity(
                    text, "amount as a number without currency");

            if (sourceId == null)
                return "Please provide the source account ID.";
            if (destId == null)
                return "Please provide the destination account ID.";
            if (amountStr == null)
                return "Please provide the amount to transfer.";

            double amount = Double.parseDouble(amountStr);
            String result = bankingApiClient.transfer(
                    sourceId, destId, amount, "Transfer via chatbot");

            return result.equals("success")
                    ? ragService.formatOperationResult(
                            "TRANSFER", sourceId, amount, true, null)
                    : ragService.formatOperationResult(
                            "TRANSFER", sourceId, amount, false,
                            result.replace("error:", ""));

        } catch (NumberFormatException e) {
            return "❌ Invalid amount. Please provide a valid number.";
        } catch (Exception e) {
            log.error("Transfer failed: {}", e.getMessage());
            return "❌ Transfer failed: " + e.getMessage();
        }
    }

    private String handleClearHistory(Long chatId) {
        conversationMemory.clearHistory(chatId);
        return "🗑 Conversation history cleared. Starting fresh!";
    }

    private String handleUnknown(Long chatId, String text) {
        return handleFaq(chatId, text);
    }

    private void sendMessage(Long chatId, String text) {
        try {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .parseMode("Markdown")
                    .build();
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to {}: {}",
                    chatId, e.getMessage());
            try {
                SendMessage plain = SendMessage.builder()
                        .chatId(chatId.toString())
                        .text(text.replaceAll("[*_`]", ""))
                        .build();
                telegramClient.execute(plain);
            } catch (TelegramApiException ex) {
                log.error("Retry also failed: {}", ex.getMessage());
            }
        }
    }
}
