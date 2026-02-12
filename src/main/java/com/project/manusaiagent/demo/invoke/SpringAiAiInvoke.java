package com.project.manusaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Spring AI Frame calls AI large models (Alibaba)
 */
// When you cancel the comment, it is executed when the project starts
@Component
public class SpringAiAiInvoke implements CommandLineRunner {

    @Resource
    private ChatModel dashscopeChatModel;

    @Override
    public void run(String... args) throws Exception {

        System.out.println("ChatModel impl = " + dashscopeChatModel.getClass().getName());

        AssistantMessage assistantMessage = dashscopeChatModel.call(new Prompt("hello, i'm an ai-agent"))
                .getResult()
                .getOutput();
        System.out.println(assistantMessage.getText());


    }


}
