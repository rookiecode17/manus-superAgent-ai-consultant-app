package com.project.manusaiagent;

import org.springframework.ai.mcp.client.common.autoconfigure.McpClientAutoConfiguration;
import org.springframework.ai.mcp.client.common.autoconfigure.McpToolCallbackAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ManusAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManusAiAgentApplication.class, args);
    }

}
