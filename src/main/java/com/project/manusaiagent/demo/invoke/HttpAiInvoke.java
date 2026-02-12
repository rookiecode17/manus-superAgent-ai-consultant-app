package com.project.manusaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * HTTP method call AI
 */
public class HttpAiInvoke {

    public static void main(String[] args) {
        // API
        String apiKey = TestApiKey.API_KEY;

        // Build request URL
        String url = "https://dashscope-intl.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

        // Build request JSON data
        JSONObject inputJson = new JSONObject();
        JSONObject messagesJson = new JSONObject();

        // Add system message
        JSONObject systemMessage = new JSONObject();
        systemMessage.set("role", "system");
        systemMessage.set("content", "You are a helpful programming assistant. You are helping build an ai agent.");

        // Add user message
        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", "who are you?");

        // Assemble messages array
        messagesJson.set("messages", JSONUtil.createArray().set(systemMessage).set(userMessage));

        // Build parameters
        JSONObject parametersJson = new JSONObject();
        parametersJson.set("result_format", "message");

        // Build complete request body
        JSONObject requestJson = new JSONObject();
        requestJson.set("model", "qwen3-max-2026-01-23");
        requestJson.set("input", messagesJson);
        requestJson.set("parameters", parametersJson);

        // Send request
        String result = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestJson.toString())
                .execute()
                .body();

        // Output result
        System.out.println(result);
    }
}