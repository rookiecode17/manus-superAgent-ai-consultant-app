package com.project.manusaiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * ReAct (Reasoning and Acting) Proxy abstraction class for patterns
 * The circular mode of thinking action is realized
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent {

    /**
     * Deal with the current state and decide on the next move
     *
     * @return Whether an action is required or not, true means it needs to be executed, and false means it doesn't need to be executed
     */
    public abstract boolean think();

    /**
     * Actions to implement the decision
     *
     * @return Results of action execution
     */
    public abstract String act();

    /**
     * Perform a single step: think and act
     *
     * @return Step execution result
     */
    @Override
    public String step() {
        try {
            // Think first
            boolean shouldAct = think();
            if (!shouldAct) {
                return "think done no action required";
            }
            // Act again
            return act();
        } catch (Exception e) {
            // Log the anomaly log
            e.printStackTrace();
            return "Step execution failed：" + e.getMessage();
        }
    }

}
