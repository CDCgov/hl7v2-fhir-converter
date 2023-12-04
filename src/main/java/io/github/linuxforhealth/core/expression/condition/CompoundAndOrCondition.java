package io.github.linuxforhealth.core.expression.condition;

import com.google.common.base.Preconditions;
import io.github.linuxforhealth.api.Condition;
import io.github.linuxforhealth.api.EvaluationResult;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompoundAndOrCondition implements Condition {
    private final String conditionStatement;

    public CompoundAndOrCondition(String conditionStatement) {
        this.conditionStatement = conditionStatement;
    }

    @Override
    public boolean test(Map<String, EvaluationResult> contextVariables) {
        return Boolean.parseBoolean(makeRecursive(conditionStatement, contextVariables));
    }

    private String makeRecursive(String conditionStatementContainer, Map<String, EvaluationResult> contextVariables) {
        // will not handle nested parens
        Pattern pattern = Pattern.compile("\\(.*?\\)");
        Matcher matcher = pattern.matcher(conditionStatementContainer);

        while (matcher.find()) {
            String group = matcher.group();
            conditionStatementContainer = eval(conditionStatementContainer, group, contextVariables);
        }

        while(conditionStatementContainer.contains("&&") || conditionStatementContainer.contains("||")) {
            if(conditionStatementContainer.contains("&&") && conditionStatementContainer.contains("||") && !conditionStatementContainer.contains("(")) {
                conditionStatementContainer = fixParens(conditionStatementContainer);
                conditionStatementContainer = makeRecursive(conditionStatementContainer, contextVariables);
            } else {
                conditionStatementContainer = eval(conditionStatementContainer, conditionStatementContainer, contextVariables);
            }
        }

        return conditionStatementContainer;
    }

    private String fixParens(String conditionStatement) {
        int orIndex = conditionStatement.indexOf("||");
        int andIndex = conditionStatement.indexOf("&&");
        if(orIndex > andIndex) {
            conditionStatement = "(" + conditionStatement.substring(0, orIndex-2) + ") " + conditionStatement.substring(orIndex, conditionStatement.length());
        } else {
            conditionStatement = conditionStatement.substring(0, orIndex+2) + " (" + conditionStatement.substring(orIndex+2, conditionStatement.length()) + ")";
        }
        return conditionStatement;
    }

    private String eval(String conditionStatement, String group, Map<String, EvaluationResult> contextVariables) {
        boolean goodToGo = false;
        if(group.contains("||")) {
            ArrayList<Condition> orString = new ArrayList<>();
            orString.add(ConditionUtil.createCondition(group.replaceAll("\\(", "").replaceAll("\\)", "")));
            CompoundORCondition compoundORCondition = new CompoundORCondition(orString);
            goodToGo = compoundORCondition.test(contextVariables);
            conditionStatement = conditionStatement.replace(group, Boolean.toString(goodToGo));
        } else if(group.contains("&&")) {
            ArrayList<Condition> andString = new ArrayList<>();
            andString.add(ConditionUtil.createCondition(group.replaceAll("\\(", "").replaceAll("\\)", "")));
            CompoundAndCondition compoundAndCondition = new CompoundAndCondition(andString);
            goodToGo = compoundAndCondition.test(contextVariables);
            conditionStatement = conditionStatement.replace(group, Boolean.toString(goodToGo));
        }

        return conditionStatement;
    }
}
