package io.github.linuxforhealth.core.expression.condition;

import io.github.linuxforhealth.api.Condition;
import io.github.linuxforhealth.api.EvaluationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompoundAndOrCondition implements Condition {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompoundAndOrCondition.class);
    private final String conditionStatement;
    private static final Pattern pattern = Pattern.compile("\\(.*?\\)");
    private final List<Condition> conditions;

    /**
     * Constructor
     * @param conditionStatement - the boolean expression to be evaluated.
     */

    public CompoundAndOrCondition(String conditionStatement, List<Condition> conditions) {
        this.conditionStatement = conditionStatement;
        this.conditions = conditions;
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

    public List<Condition> getConditions() {
        return this.conditions;
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
        if(group.contains("||")) {
            conditionStatementContainer = cleanupAndTest(group, contextVariables, conditionStatementContainer,
                    CompoundORCondition.class);
        } else if(group.contains("&&")) {
            conditionStatementContainer = cleanupAndTest(group, contextVariables, conditionStatementContainer,
                    CompoundAndCondition.class);
        }

        return conditionStatementContainer;
    }

    /**
     * Removes the parens so that we can evaluate either the "AND" or "OR" condition.
     * Will return false if an exception is thrown since otherwise the code will go on infinitely.
     * @param group - Holds the current expression to be evaluated
     * @param contextVariables - The names and values of any variables that may exist within the conditionStatement
     * @param conditionStatementContainer - Holds the current state of the conditionStatement
     * @param conditionClass - The class (ComoundAndCondition or CompoundOrCondition) to call the test method on.
     * @return returns the current state of the ConditionStatement
     */
    private String cleanupAndTest(String group, Map<String, EvaluationResult> contextVariables,
                                  String conditionStatementContainer, Class<?> conditionClass) {
        boolean goodToGo;
        ArrayList<Condition> expressionString = new ArrayList<>();
        expressionString.add(ConditionUtil.createCondition(group.replaceAll("\\(", "").replaceAll("\\)", "")));

        try {
            Condition compoundCondition = (Condition) conditionClass.getDeclaredConstructor(List.class).newInstance(expressionString);
            goodToGo = compoundCondition.test(contextVariables);
            return conditionStatementContainer.replace(group, Boolean.toString(goodToGo));
        } catch (NoSuchMethodException e) {
            LOGGER.error("Could not find the constructor method for class " + conditionClass);
            return "false";
        } catch(InvocationTargetException e) {
            LOGGER.error("Could not find the invoke the constructor method for class " + conditionClass);
            return "false";
        } catch(InstantiationException e) {
            LOGGER.error("Could not find the instantiate class " + conditionClass);
            return "false";
        } catch (IllegalAccessException e) {
            LOGGER.error("Could not access the constructor method for class " + conditionClass);
            return "false";
        }
    }

    @Override
    public String toString() {
        return "CompoundAndOrCondition(" +
                "conditionStatement='" + conditionStatement + '\'' +
                ')';
    }
}
