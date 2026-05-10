package hattabi.youness.ebanking_chatbot.rag;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentIngestionService {
    private final SimpleVectorStore vectorStore;

    @PostConstruct
    public void ingestDocuments() {
        log.info("Sarting document ingestion into vector store...");
        try {
            ClassPathResource resource = new ClassPathResource("docs/banking-faq.txt");
            TextReader textReader = new TextReader(resource);
            textReader.getCustomMetadata().put("source", "banking-faq");

            List<Document> rawDocuments = textReader.get();
            TokenTextSplitter splitter = new TokenTextSplitter(
                    500,
                    100,
                    5,
                    10000,
                    true);

            List<Document> chunks = splitter.apply(rawDocuments);

            vectorStore.add(chunks);
            log.info("Ingested {} document chunks into vector store", chunks.size());
        } catch (Exception e) {
            log.error("Failed to ingest documents: {}", e.getMessage());
        }
    }
}
