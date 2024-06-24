package com.maestreaux.dynasties.core.goap;

import java.util.List;

public class Goal {
    private final List<GoalCondition> conditions;
    private boolean persistent = false;

    public Goal(List<GoalCondition> conditions) {
        this.conditions = conditions;
    }
    public Goal(List<GoalCondition> conditions, boolean persistent) {
        this(conditions);
        this.persistent = persistent;
    }

    public boolean isPersistent() { return this.persistent; }

    public List<GoalCondition> getConditions() {
        return conditions;
    }
}
