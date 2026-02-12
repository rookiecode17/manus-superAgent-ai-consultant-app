package com.project.manusaiagent.rag;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
//import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;

/**
 * Love Guru Vector Store Configuration （In-Memory Bean Initialization）
 */
@Configuration
@Slf4j
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Value("${spring.ai.dashscope.api-key:NOT_FOUND}")
    private String apiKey;

    @Bean
    VectorStore loveAppVectorStore(@Lazy @Qualifier("dashscopeEmbeddingModel") EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
//         Load the document
        List<Document> documentList = loveAppDocumentLoader.loadMarkdowns();


        // Autonomous slicing of documents
//        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documentList);
        // Automatically Supplement Keyword Meta Information
        List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(documentList);
//        simpleVectorStore.add(enrichedDocuments);
//        simpleVectorStore.add(splitDocuments);

        return simpleVectorStore;


    }
}
