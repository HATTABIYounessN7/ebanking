package hattabi.youness.ebanking_chatbot.config;

import java.util.List;
import java.util.Map;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hattabi.youness.ebanking_chatbot.rag.RagService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TestController {
        private final SimpleVectorStore vectorstore;
        private final RagService ragService;

        @GetMapping("/test/search")
        public List<Map<String, Object>> testSearch(@RequestParam String query) {
                return vectorstore.similaritySearch(
                                SearchRequest.builder()
                                                .query(query)
                                                .topK(3)
                                                .build())
                                .stream()
                                .map(doc -> Map.of(
                                                "content", doc.getText(),
                                                "score", doc.getMetadata()
                                                                .getOrDefault("distance", "N/A")))
                                .toList();
        }

        @GetMapping("/test/rag")
        public Map<String, String> testRag(
                        @RequestParam String question) {

                String answer = ragService.askWithContext(question, 0L);
                return Map.of(
                                "question", question,
                                "answer", answer);
        }
}
