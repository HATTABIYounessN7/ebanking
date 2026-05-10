package hattabi.youness.ebanking_chatbot.rag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class ConversationMemory {
    private final Map<Long, List<Message>> conversations = new ConcurrentHashMap<>();

    private static final int MAX_HISTORY = 10;

    public record Message(String role, String content) {
    }

    public void addMessage(Long chatId, String role, String content) {
        conversations.computeIfAbsent(chatId, k -> new ArrayList<>())
                .add(new Message(role, content));

        List<Message> history = conversations.get(chatId);
        if (history.size() > MAX_HISTORY * 2) {
            conversations.put(chatId,
                    new ArrayList<>(
                            history.subList(history.size() - MAX_HISTORY * 2,
                                    history.size())));
        }
    }

    public List<Message> getHistory(Long chatId) {
        return conversations.getOrDefault(chatId, new ArrayList<>());
    }

    public void clearHistory(Long chatId) {
        conversations.remove(chatId);
    }

    public String formatHistoryAsContext(Long chatId) {
        List<Message> history = getHistory(chatId);
        if (history.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append("Previous conversation:\n");
        history.forEach(msg -> sb.append(msg.role().equals("user") ? "User: " : "Assistant: ")
                .append(msg.content())
                .append("\n"));
        return sb.toString();
    }
}
