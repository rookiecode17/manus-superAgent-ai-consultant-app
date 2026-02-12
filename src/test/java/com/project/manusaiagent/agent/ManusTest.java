package com.project.manusaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ManusTest {

    @Resource
    private Manus manus;

    @Test
    public void run() {
        String userPrompt = """
               Your partner will visit sacramento, CA, please help him find a suitable date location within 5 kilometers,
               And combined with some online pictures, make a detailed dating plan,
               And output in PDF format""";
        String answer = manus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}