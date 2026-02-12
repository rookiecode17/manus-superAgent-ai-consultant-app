package com.project.manusaiagent.rag;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * Create a custom RAG retrieval augmentation advisor's factory
 */
public class LoveAppRagCustomAdvisorFactory {

    /**
     * Create a custom RAG retrieval enhancement advisor
     *
     * @param vectorStore Vector storage
     * @param status      state
     * @return Custom RAG retrieval enhancement advisor
     */
    public static Advisor createLoveAppRagCustomAdvisor(VectorStore vectorStore, String status) {
        // Filter documents for specific states
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();
        // Create a document retriever
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
//                .filterExpression(expression) // Filter conditions
                .similarityThreshold(0.5) // Similarity threshold
                .topK(3) //Return the number of documents
                .build();
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(LoveAppContextualQueryAugmenterFactory.createInstance())
                .build();
    }
}
