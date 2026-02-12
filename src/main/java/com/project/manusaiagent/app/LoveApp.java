package com.project.manusaiagent.app;


import com.project.manusaiagent.advisor.MyLoggerAdvisor;
import com.project.manusaiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.project.manusaiagent.rag.QueryRewriter;
import io.swagger.v3.core.filter.SpecFilter;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@Slf4j
public class LoveApp {


    private final ChatClient chatClient;
    private static final String SYSTEM_PROMPT =
            "Act as an expert in romantic psychology with extensive experience in relationship counseling. " +
                    "Start by introducing your role to the user and let them know they can share their relationship concerns with you. " +
                    "Ask questions based on three relationship statuses: single, in a relationship, and married. " +
                    "For single users, ask about expanding their social circle and difficulties in pursuing someone they are interested in. " +
                    "For users in a relationship, ask about communication issues and conflicts caused by differences in habits or expectations. " +
                    "For married users, ask about handling family responsibilities and managing relationships with relatives. " +
                    "Encourage the user to describe the situation in detail, including what happened, how the other person reacted, and their own thoughts and feelings, " +
                    "so that you can provide personalized advice and solutions.";
    @Autowired
    private SpecFilter specFilter;

    public LoveApp(ChatModel dashscopeChatModel) {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(3)
                .build();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private VectorStore pgVectorVectorStore;

//    @Resource
//    private Advisor loveAppRagCloudAdvisor;

    @Resource
    private QueryRewriter queryRewriter;

    /**
     * Engage in conversations with the RAG Knowledge Base
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {

//         query rewrite
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);

        ChatResponse chatResponse = chatClient
                .prompt()
                // Use the rephrased query
                .user(rewrittenMessage )
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, chatId)
                        .advisors(
                                new MyLoggerAdvisor(),
//                                QuestionAnswerAdvisor.builder(loveAppVectorStore).build(),
//                                QuestionAnswerAdvisor.builder(pgVectorVectorStore).build()
                                LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(
                                        loveAppVectorStore,
                                        "answersForSingles"
                                )
                        )
                )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;

    }

    // AI invoke tool capabilities
    @Resource
    private ToolCallback[] allTools;

    /**
     * AI Relationship Report Feature (Support Calling Tool)
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithTools(String message, String chatId) {
        log.info("chatId={}, userLen={}, userHead={}",
                chatId,
                message == null ? -1 : message.length(),
                message == null ? "null" : message.substring(0, Math.min(80, message.length()))
        );


        if (message == null || message.trim().isEmpty()) {
            return "Message must not be empty.";
        }
        if (message.length() > 8000) {
            message = message.substring(0, 8000) + "\n...[TRUNCATED]";
        }

        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // Open the log for easy observation of the effect
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    // AIInvoke the MCP service

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    /**
     * AI Love Reporting Feature (Calling MCP Service)
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithMcp(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // Open the log for easy observation of the effect
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * AI based conversations (support multi-round conversation memory, SSE streaming)
     *
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    record LoveReport(String title, List<String> suggestions) {

    }

    /**
     * AI Love report function (actual structured output)
     *
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "After each conversation, a relationship result is generated, titled {username}, and a list of suggestions")
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }


}