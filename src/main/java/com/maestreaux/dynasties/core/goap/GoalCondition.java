package com.maestreaux.dynasties.core.goap;

import java.util.Objects;
import java.util.function.Predicate;

public record GoalCondition(String id) {
    public static final GoalCondition HAS_LARVA = new GoalCondition("has_larva");
    public static final GoalCondition GO_TO_LARVA = new GoalCondition("go_to_larva");
    public static final GoalCondition PLANTS_MUCUS = new GoalCondition("plants_mucus");

    @Override
    public boolean equals(Object object) {
        return object instanceof GoalCondition requirement && Objects.equals(this.id, requirement.id);
    }

    public <T> GoalState<T> setGoalStatePredicate(Predicate<T> predicate) {
        return new GoalState<>(this, predicate);
    }
}

