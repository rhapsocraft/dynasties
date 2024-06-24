package com.maestreaux.dynasties.core.goap;

import com.maestreaux.dynasties.core.goap.interfaces.IGoalAction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActionGraph<T> {

    private final List<IGoalAction<T>> actions;
    private final HashMap<GoalCondition, List<ActionNode<T>>> actionNodesMap = new HashMap<>();

    public ActionGraph(List<IGoalAction<T>> actions ) {
        this.actions = actions;
        graphBehaviourNodes();
    }

    public List<IGoalAction<T>> getActions() {
        return this.actions;
    }

    private List<OpenedNode<T>> openNodes(List<ActionNode<T>> actionNodes) {
        return actionNodes.stream().map(node -> new OpenedNode<>(node, null)).toList();
    }

    @Nullable
    public ActionSolution<T> traverseFromRequirements(@NotNull List<GoalCondition> conditions) {
        // TODO: Implement Binary Heap for optimization
        List<OpenedNode<T>> openSet = new ArrayList<>();

        if (!conditions.isEmpty()) {
            // Iterate all requirements to find applicable behaviours
            for (var condition: conditions) {
                // Open all nodes which support the conditions
                var rootNodes = openNodes(this.actionNodesMap.get(condition));
                openSet.addAll(rootNodes);
            }
        }

        do {
            var _node = openSet.stream().min((node1, node2) -> Float.compare(node1.getGCost(), node2.getGCost()));

            if (_node.isPresent()) {
                var node = _node.get();
                var openedNeighbours = node.openNeighbours();

                if (openedNeighbours.isEmpty()) {
                    return new ActionSolution<T>(node);
                } else {
                    openSet.remove(node);
                    openSet.addAll(openedNeighbours);
                }
            }

        } while(!openSet.isEmpty());

        return null;
    }

    private void graphBehaviourNodes() {
        ActionNode<T>[] nodes = new ActionNode[this.actions.size()];

        for (int index = 0; index < nodes.length; index++) {
            nodes[index] =  new ActionNode<>(this.actions.get(index));
        }

        for (int index1 = 0; index1 < nodes.length; index1++) {
            var node1 = nodes[index1];

            // Map this node based on the requirements that they can fulfill
            var requirementsFulfilled = node1.getAction().getEffects();
            for (var requirement : requirementsFulfilled) {
                var condition = requirement.getGoalCondition();
                var mappedBehaviours = this.actionNodesMap.get(condition);

                // TODO: Group nodes if they have the same fulfillments and conditions

                if (mappedBehaviours != null) {
                    mappedBehaviours.add(node1);
                } else {
                    this.actionNodesMap.put(condition, ObjectArrayList.of(node1));
                }
            }

            for (int index2 = 0; index2 < nodes.length; index2++) {
                if (index1 != index2) {
                    var node2 = nodes[index2];
                    var fulfilledRequirements = node1.getAction().getPreconditionsFulfilledBy(node2.getAction());

                    if (!fulfilledRequirements.isEmpty()) {
                        node1.addNeighbour(node2);
                    }
                }
            }
        }
    }

    public static class ActionSolution<T> {
        private final OpenedNode<T> startNode;
        private final OpenedNode<T> endNode;

        public ActionSolution(OpenedNode<T> startNode) {
            this.startNode = startNode;

            var node = startNode;
            while (node.getParent() != null) {
                node = node.getParent();
            }

            this.endNode = node;
        }

        public OpenedNode<T> getStartNode() {
            return this.startNode;
        }

        public OpenedNode<T> getEndNode() {
            return this.endNode;
        }
    }

    public static class OpenedNode<T> {
        private final OpenedNode<T> parent;
        private final ActionNode<T> baseNode;

        public OpenedNode(ActionNode<T> baseNode, OpenedNode<T> parent) {
            this.baseNode = baseNode;
            this.parent = parent;
        }

        public float getGCost() {
            var parentCost = this.parent != null ? this.parent.getGCost() : 0F;

            return parentCost + this.baseNode.getAction().getCost();
        }

        public List<OpenedNode<T>> openNeighbours() {
            return this.baseNode.getNeighbours().stream().map(node -> new OpenedNode<>(node, this)).toList();
        }

        public IGoalAction<T> getAction() {
            return this.baseNode.getAction();
        }

        public OpenedNode<T> getParent() {
            return this.parent;
        }
    }
}
