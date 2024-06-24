package com.maestreaux.dynasties.core.goap;

import java.util.function.Predicate;

public class GoalState<T> {
    private final GoalCondition goalCondition;
    private final Predicate<T> fulfilledPredicate;

    public GoalState(GoalCondition condition, Predicate<T> fulfilledPredicate) {
        this.goalCondition = condition;
        this.fulfilledPredicate = fulfilledPredicate;
    }

    public GoalCondition getGoalCondition() {
        return this.goalCondition;
    }

    public boolean isFulfilled(T param) {
        return this.fulfilledPredicate.test(param);
    }
}
