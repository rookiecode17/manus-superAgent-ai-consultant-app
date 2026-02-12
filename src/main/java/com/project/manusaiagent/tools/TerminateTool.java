package com.project.manusaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;

/**
 * Termination tool (which allows the autonomous planning agent to reasonably interrupt)
 */
public class TerminateTool {

    @Tool(description = """
            Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.
            "When you have finished all the tasks, call this tool to end the work.
            """)
    public String doTerminate() {
        return "Mission over";
    }
}
