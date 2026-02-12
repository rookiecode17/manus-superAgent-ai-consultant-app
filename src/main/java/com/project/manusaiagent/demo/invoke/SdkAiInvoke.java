package com.project.manusaiagent.demo.invoke;// Use DashScope SDK V2.12.0 or later.
import java.util.Arrays;
import java.lang.System;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;

public class SdkAiInvoke {
    static {
        Constants.baseHttpApiUrl="https://dashscope-intl.aliyuncs.com/api/v1";
    }
    public static GenerationResult callWithMessage() throws ApiException, NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("You are a helpful programming assistant. You are helping build an ai agent.")
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content("Who are you?")
                .build();
        GenerationParam param = GenerationParam.builder()
                // If the environment variable is not set, replace it with your Model Studio API key: .apiKey("sk-xxx")
                .apiKey(TestApiKey.API_KEY)
                .model("qwen3-max-2026-01-23")
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
//                .enableThinking(true)
                .build();
        return gen.call(param);
    }
    public static void main(String[] args) {
        try {
            GenerationResult result = callWithMessage();
            System.out.println("====================Thinking process====================");
            System.out.println(result.getOutput().getChoices().get(0).getMessage().getReasoningContent());
            System.out.println("\n====================Full response====================");
            System.out.println(result.getOutput().getChoices().get(0).getMessage().getContent());
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            System.err.println("Error message: "+e.getMessage());
        }
    }
}