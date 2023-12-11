package io.github.linuxforhealth.core.expression.condition;

import io.github.linuxforhealth.api.Condition;
import io.github.linuxforhealth.api.EvaluationResult;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompoundAndOrCondition implements Condition {
    private final String conditionStatement;
    private static final Pattern pattern = Pattern.compile("\\(.*?\\)");

    /**
     * Constructor
     * @param conditionStatement - the boolean expression to be evaluated.
     */

    public CompoundAndOrCondition(String conditionStatement) {
        this.conditionStatement = conditionStatement;
    }

    /**
     * Tests whether the conditionStatement evaluates to true of false.
     * @param contextVariables - Map of String, {@link EvaluationResult} - The names and values of any variables that
     *                         may exist within the conditionStatement
     * @return returns a boolean expressing whether the condition evaluated to true or false.
     */
    @Override
    public boolean test(Map<String, EvaluationResult> contextVariables) {
        return Boolean.parseBoolean(makeRecursive(conditionStatement, contextVariables));
    }

    /**
     * A function that will be called repeatedly until each part of the boolean expression has been evaluated down to a
     * true or false and combined to one singular boolean value
     * @param conditionStatementContainer - Holds the current state of the conditionStatement
     * @param contextVariables - The names and values of any variables that may exist within the conditionStatement
     * @return returns the current state of the ConditionStatement
     */
    private String makeRecursive(String conditionStatementContainer, Map<String, EvaluationResult> contextVariables) {
        // will not handle nested parens
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

    /**
     * We need consistent parenthesis to detect each piece of the statement. This adds those in so that each piece will
     * be properly grouped and evaluated. ex. $var1 EQUALS abc || $var2 EQUALS xyz && $var1 NOT_NULL becomes
     * $var1 EQUALS abc || ($var2 EQUALS xyz && $var1 NOT_NULL)
     * @param conditionStatementContainer - Holds the current state of the conditionStatement
     * @return returns the current state of the ConditionStatement
     */
    private String fixParens(String conditionStatementContainer) {
        int orIndex = conditionStatementContainer.indexOf("||");
        int andIndex = conditionStatementContainer.indexOf("&&");
        if(orIndex > andIndex) {
            conditionStatementContainer = "(" + conditionStatementContainer.substring(0, orIndex-2) + ") " + conditionStatementContainer.substring(orIndex, conditionStatementContainer.length());
        } else {
            conditionStatementContainer = conditionStatementContainer.substring(0, orIndex+2) + " (" + conditionStatementContainer.substring(orIndex+2, conditionStatementContainer.length()) + ")";
        }
        return conditionStatementContainer;
    }

    /**
     * Evaluates the current expression, once it is broken down into one of the existing types of supported expressions.
     * @param conditionStatementContainer - Holds the current state of the conditionStatement
     * @param group - Holds the current expression to be evaluated
     * @param contextVariables - The names and values of any variables that may exist within the conditionStatement
     * @return returns the current state of the ConditionStatement
     */
    private String eval(String conditionStatementContainer, String group, Map<String, EvaluationResult> contextVariables) {
        boolean goodToGo;
        if(group.contains("||")) {
            ArrayList<Condition> orString = new ArrayList<>();
            orString.add(ConditionUtil.createCondition(group.replaceAll("\\(", "").replaceAll("\\)", "")));
            CompoundORCondition compoundORCondition = new CompoundORCondition(orString);
            goodToGo = compoundORCondition.test(contextVariables);
            conditionStatementContainer = conditionStatementContainer.replace(group, Boolean.toString(goodToGo));
        } else if(group.contains("&&")) {
            ArrayList<Condition> andString = new ArrayList<>();
            andString.add(ConditionUtil.createCondition(group.replaceAll("\\(", "").replaceAll("\\)", "")));
            CompoundAndCondition compoundAndCondition = new CompoundAndCondition(andString);
            goodToGo = compoundAndCondition.test(contextVariables);
            conditionStatementContainer = conditionStatementContainer.replace(group, Boolean.toString(goodToGo));
        }

        return conditionStatementContainer;
    }
}
