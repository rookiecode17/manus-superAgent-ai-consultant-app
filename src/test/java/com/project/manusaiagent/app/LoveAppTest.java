package com.project.manusaiagent.app;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class LoveAppTest {


    @Resource
    private LoveApp loveApp;


    @Value("${spring.ai.dashscope.api-key:NOT_FOUND}")
    String key;



//    @Test
//    void checkKey() {
//        System.out.println("dashscope key loaded? " + !"NOT_FOUND".equals(key));
//    }
@Test
void keyShouldBeLoaded() {
    Assertions.assertNotEquals("NOT_FOUND", key);
    Assertions.assertTrue(key.length() > 10);
    System.out.println("dashscope key loaded? true");
}

    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();

        String message = "";
        String answer = loveApp.doChat(message, chatId);

        message = "";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);

        message = "";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }


    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "";
        String answer = loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {
//         Test answers to your online search questions
//        testMessage("If you want to take your girlfriend to Shanghai on a date on the weekend, how many niche check-in places are recommended for couples?");

//         Testing Web Scraping: A Love Case Study
//        testMessage("Recently quarreled with the object, see how other couples resolve conflicts?");

//         Test resource download: Image download
//        testMessage("Directly download a cat picture suitable for mobile phone wallpaper as a file");
//        testMessage("Call downloadResource with this URL: https://images.unsplash.com/photo-... and fileName cat.jpg");

//        Test terminal action: Execute the code
//        testMessage("Execute Python3 script to generate data analysis report");

//         Test file operation: Save the user profile
//        testMessage("Save my love profile as a file");

//         Test PDF generation
        testMessage("Generate a Valentine plan with restaurant reservations and generate a pdf file to save it, event process, and gift list");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
//        Test mapMCP
//        String message = "your friends will visit san francisco, ca recently. Could you suggest some family friendly spots within 2 km that we could visit and come back the same day? You must use the map tool; do not answer from general knowledge. Return at most 5 places.\n" +
//                "Assume the center point is downtown san francisco, CA.\n" +
//                "\n" +
//                "Use the map tool `find_nearby_places` exactly once.\n" +
//                "Search good place to go for my parents using these keywords/categories:\n" +
//                "park, museum, arboretum, mountain, shopping mall, nature, lake.\n" +
//                "\n" +
//                "Return at most 5 places total.\n" +
//                "For each place, include ONLY: name, type/category, distance_km, address, and a 1-sentence reason.\n" +
//                "Do not include raw JSON. If there are no good results, say so and suggest the next best nearby options.";

        // Test image search MCP
        String message = "find some pics of cats";

        String answer =  loveApp.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(answer);
    }
}