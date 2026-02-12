package com.project.manusaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

public class LangChainAiInvoke {

    public static void main(String[] args) {
        ChatLanguageModel qwenChatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .baseUrl("https://dashscope-intl.aliyuncs.com/api/v1")
                .modelName("qwen3-max-2026-01-23")
                .build();
        String answer = qwenChatModel.chat("i'm building ai-agent");
        System.out.println(answer);
    }
}
