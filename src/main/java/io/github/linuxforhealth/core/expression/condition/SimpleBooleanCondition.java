package io.github.linuxforhealth.core.expression.condition;

import io.github.linuxforhealth.api.Condition;
import io.github.linuxforhealth.api.EvaluationResult;

import java.util.Map;

public class SimpleBooleanCondition implements Condition {
    private final String conditionStatement;

    public SimpleBooleanCondition(String conditionStatement) {
        this.conditionStatement = conditionStatement;
    }

    @Override
    public boolean test(Map<String, EvaluationResult> contextVariables) {
        return Boolean.parseBoolean(conditionStatement);
    }
}
