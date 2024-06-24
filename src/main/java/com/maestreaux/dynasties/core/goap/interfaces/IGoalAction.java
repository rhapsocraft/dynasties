package com.maestreaux.dynasties.core.goap.interfaces;

import com.maestreaux.dynasties.core.goap.GoalCondition;
import com.maestreaux.dynasties.core.goap.GoalState;

import java.util.List;

public interface IGoalAction<T> {

    List<GoalCondition> getPreconditions();
    List<GoalState<T>> getEffects();

    default boolean fulfilledBy(IGoalAction<T> otherBehaviour) {
        var thisConditions = getPreconditions();
        var otherConditions = otherBehaviour.getPreconditions();

        return otherConditions.stream().anyMatch(thisConditions::contains);
    }

    default List<GoalCondition> getPreconditionsFulfilledBy(IGoalAction<T> otherBehaviour) {
        var otherEffects = otherBehaviour.getEffects().stream().map(GoalState::getGoalCondition).toList();
        return this.getPreconditions().stream().filter(otherEffects::contains).toList();
    }

    default float getCost() {
        return 1F;
    }

    default boolean isAllEffectsFulfilled(T param) {
        return this.getEffects().stream().allMatch(effect -> effect.isFulfilled(param));
    }
}
