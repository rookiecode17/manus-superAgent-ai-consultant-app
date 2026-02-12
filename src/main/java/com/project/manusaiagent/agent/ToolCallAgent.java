package com.project.manusaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.project.manusaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The underlying proxy class that handles tool calls implements the think and act methods and can be used as a parent class for creating instances
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    // Available tools
    private final ToolCallback[] availableTools;

    // Save the response results of tool call information (to call those tools)
    private ChatResponse toolCallChatResponse;

    // Tool call manager
    private final ToolCallingManager toolCallingManager;

    // Disable Spring AI's built-in tool calling mechanism and maintain options and message context yourself
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // Disable Spring AI's built in tool calling mechanism and maintain options and message context yourself
        this.chatOptions = DashScopeChatOptions.builder()
                .withInternalToolExecutionEnabled(false)
                .build();
    }

    /**
     * Deal with the current state and decide on the next move
     *
     * @return whether An Action Is Required
     */
    @Override
    public boolean think() {
        // 1、Verify prompts and splice user prompts
        if (StrUtil.isNotBlank(getNextStepPrompt())) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        // 2、Call the AI large model to get the results of the tool call
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, this.chatOptions);
        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
//                    .tools(availableTools)
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            // Record the response for waiting for the next Act
            this.toolCallChatResponse = chatResponse;
            // 3、Parse the result of the tool call to get the tool you want to call
            // Assistant messages
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // Get a list of tools to call
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            // Output prompt information
            String result = assistantMessage.getText();
            log.info(getName() + "thinking：" + result);
            log.info(getName() + "chosen " + toolCallList.size() + " tools");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("Tool Name：%s，parameters：%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            // If you don't need to call the tool, go back false
            if (toolCallList.isEmpty()) {
                // Only when the tool is not invoked does it need to be manually recorded
                getMessageList().add(assistantMessage);
                return false;
            } else {
                // When you need to invoke a tool, you don't need to record the assistant message, because it is automatically recorded when the tool is invoked
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "The thinking process encountered problems：" + e.getMessage());
            getMessageList().add(new AssistantMessage("Encountered an error while processing：" + e.getMessage()));
            return false;
        }
    }

    /**
     * Execute tool calls and process the results
     *
     * @return Execution results
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "There are no tools to call";
        }
        // Call the tool
        Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // Log message context，conversationHistory Already contains the results returned by the helper message and the tool call
        setMessageList(toolExecutionResult.conversationHistory());
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        // Determine whether the termination tool is called
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> response.name().equals("doTerminate"));
        if (terminateToolCalled) {
            // The task ends, and the status is changed
            setState(AgentState.FINISHED);
        }
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "tools " + response.name() + " Returned results：" + response.responseData())
                .collect(Collectors.joining("\n"));
        log.info(results);
        return results;
    }
}
