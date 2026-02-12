package com.project.manusaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PgVectorVectorStoreConfigTest {

    @Resource
    private VectorStore pgVectorVectorStore;
    @Test
    void pgVectorVectorStore() {
        List<Document> documents = List.of(
                new Document("What is the use of the Love guru app? Your personal relationship coach.", Map.of("meta1", "meta1")),
                new Document("Relationship Coaching Help"),
                new Document("what a guru", Map.of("meta2", "meta2")));
        // Add documents
        pgVectorVectorStore.add(documents);
        // Similarity query
        List<Document> results = pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("relationship help").topK(3).build());
        Assertions.assertNotNull(results);
    }
}