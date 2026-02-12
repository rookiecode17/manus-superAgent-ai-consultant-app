package com.project.manusaiagent.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

/**
 * Query the rewriter
 */
@Component
public class QueryRewriter {

    private final QueryTransformer queryTransformer;

    public QueryRewriter(ChatModel dashscopeChatModel) {
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
        // Create a query rewrite transformer
        queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();
    }

    /**
     * Perform query rewrites
     *
     * @param prompt
     * @return
     */
    public String doQueryRewrite(String prompt) {
        Query query = new Query(prompt);
        // Perform query rewrites
        Query transformedQuery = queryTransformer.transform(query);
        // Output the rewritten query
        return transformedQuery.text();
    }
}
