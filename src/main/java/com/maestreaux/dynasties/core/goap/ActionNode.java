package com.maestreaux.dynasties.core.goap;

import com.maestreaux.dynasties.core.goap.interfaces.IGoalAction;

import java.util.ArrayList;
import java.util.List;

public class ActionNode<T> {
    private final List<ActionNode<T>> neighbours = new ArrayList<>();
    private final IGoalAction<T> action;

    public ActionNode(IGoalAction<T> action) {
        this.action = action;
    }


    public IGoalAction<T> getAction() {
        return this.action;
    }


    public void addNeighbour(ActionNode<T> newNode) {
        neighbours.add(newNode);
    }

    public List<ActionNode<T>> getNeighbours() {
        return this.neighbours;
    }
}
