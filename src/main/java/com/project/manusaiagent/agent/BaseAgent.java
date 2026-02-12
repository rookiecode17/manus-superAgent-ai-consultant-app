package com.project.manusaiagent.agent;

import cn.hutool.core.util.StrUtil;

import com.project.manusaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract the underlying agent class to manage agent state and execution processes.
 *
 * Provides foundational functionality for state transitions, memory management, and step-based execution loops.
 * The subclass must implement the step method.
 */
@Data
@Slf4j
public abstract class BaseAgent {

    // Core attributes
    private String name;

    // Prompt words
    private String systemPrompt;
    private String nextStepPrompt;

    // Agent status
    private AgentState state = AgentState.IDLE;

    //Execution step control
    private int currentStep = 0;
    private int maxSteps = 10;

    // LLM Large model
    private ChatClient chatClient;

    // Memory (requires autonomous maintenance of session context)
    private List<Message> messageList = new ArrayList<>();

    /**
     * Run the agent
     *
     * @param userPrompt User prompts
     * @return Execution results
     */
    public String run(String userPrompt) {
        // 1、Basic calibration
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        // 2、execute change state
        this.state = AgentState.RUNNING;
        // Log message context
        messageList.add(new UserMessage(userPrompt));
        // Save the list of results
        List<String> results = new ArrayList<>();
        try {
            //Execution loop
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}", stepNumber, maxSteps);
                // single step execution
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }
            // Check if the step limit is exceeded
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("error executing agent", e);
            return "Execution error" + e.getMessage();
        } finally {
            // 3、Clean up resources
            this.cleanup();
        }
    }

    /**
     * Run agent (streaming output)
     *
     * @param userPrompt User prompts
     * @return Execution results
     */
    public SseEmitter runStream(String userPrompt) {
        // Create a one with a long timeout SseEmitter
        SseEmitter sseEmitter = new SseEmitter(300000L); // 5 minute timeout
        // Use thread asynchronous processing to avoid blocking the main thread
        CompletableFuture.runAsync(() -> {
            // 1、Basic calibration
            try {
                if (this.state != AgentState.IDLE) {
                    sseEmitter.send("Error: Unable to run agent from state:" + this.state);
                    sseEmitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    sseEmitter.send("Error: Cannot run agent with empty prompts");
                    sseEmitter.complete();
                    return;
                }
            } catch (Exception e) {
                sseEmitter.completeWithError(e);
            }
            // 2、Execute, change state
            this.state = AgentState.RUNNING;
            // Log message context
            messageList.add(new UserMessage(userPrompt));
            // Save the list of results
            List<String> results = new ArrayList<>();
            try {
                // Execution loop
                for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                    int stepNumber = i + 1;
                    currentStep = stepNumber;
                    log.info("Executing step {}/{}", stepNumber, maxSteps);
                    // Single step execution
                    String stepResult = step();
                    String result = "Step " + stepNumber + ": " + stepResult;
                    results.add(result);
                    // Outputs the results of each current step to SSE
                    sseEmitter.send(result);
                }
                // Check if the step limit is exceeded
                if (currentStep >= maxSteps) {
                    state = AgentState.FINISHED;
                    results.add("Terminated: Reached max steps (" + maxSteps + ")");
                    sseEmitter.send("Execution End: Reach the maximum step（" + maxSteps + "）");
                }
                // Normal completion
                sseEmitter.complete();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("error executing agent", e);
                try {
                    sseEmitter.send("Execution error：" + e.getMessage());
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
            } finally {
                // 3、Clean up resources
                this.cleanup();
            }
        });

        // Set a timeout callback
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timeout");
        });
        // Set up a callback
        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });
        return sseEmitter;
    }

    /**
     * Define a single step
     *
     * @return
     */
    public abstract String step();

    /**
     * Clean up resources
     */
    protected void cleanup() {
        // Subclasses can override this method to clean up resources
    }
}
