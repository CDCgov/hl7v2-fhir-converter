package io.github.linuxforhealth.core.expression.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleBooleanConditionTest {
    @Test
    void simple_condition_false() {
        SimpleBooleanCondition simpleBooleanCondition = (SimpleBooleanCondition) ConditionUtil.createCondition("false");
        assertThat(simpleBooleanCondition.test(null)).isFalse();
    }

    @Test
    void simple_condition_true() {
        SimpleBooleanCondition simpleBooleanCondition = (SimpleBooleanCondition) ConditionUtil.createCondition("true");
        assertThat(simpleBooleanCondition.test(null)).isTrue();
    }

    @Test
    void simple_condition_other() {
        SimpleBooleanCondition simpleBooleanCondition = (SimpleBooleanCondition) ConditionUtil.createCondition("other");
        assertThat(simpleBooleanCondition.test(null)).isFalse();
    }
}
