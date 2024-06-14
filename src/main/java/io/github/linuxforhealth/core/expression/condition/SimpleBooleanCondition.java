package io.github.linuxforhealth.core.expression.condition;

import io.github.linuxforhealth.api.Condition;
import io.github.linuxforhealth.api.EvaluationResult;

import java.util.Map;

public class SimpleBooleanCondition implements Condition {
    private final String conditionStatement;

    /**
     * Constructor
     * @param conditionStatement - the boolean expression to be evaluated. Should either be the string "true" or "false"
     */
    public SimpleBooleanCondition(String conditionStatement) {
        this.conditionStatement = conditionStatement;
    }

    /**
     * Tests whether the condition is true or false
     * @param contextVariables - Map of String, {@link EvaluationResult} - The names and values of any variables that
     *                         may exist within the conditionStatement
     * @return returns the boolean value of the conditionStatement.
     */
    @Override
    public boolean test(Map<String, EvaluationResult> contextVariables) {
        return Boolean.parseBoolean(conditionStatement);
    }

    @Override
    public String toString() {
        return "SimpleBooleanCondition{" +
                "conditionStatement='" + conditionStatement + '\'' +
                '}';
    }
}
