package com.maestreaux.dynasties.core.goap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.GroupBehaviour;
import net.tslat.smartbrainlib.object.SBLShufflingList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GoalPlanner<E extends LivingEntity> extends GroupBehaviour<E> {
    private final ActionGraph<E> actionGraph;
    private Goal goal;

    private ActionGraph.OpenedNode<E> primaryActionNode;
    private ActionGraph.ActionSolution<E> solution;
    private ActionGraph.OpenedNode<E> currentActionNode;

    public GoalPlanner(ActionGraph<E> actionGraph) {
        super(new ExtendedBehaviour[] {});
        this.actionGraph = actionGraph;
    }

    public GoalPlanner<E> setGoal(List<GoalCondition> conditions) {
        this.goal = new Goal(conditions);

        // TODO: Static for now, but this may change later on where best sequence of actions depends on world state
        this.solution = this.actionGraph.traverseFromRequirements(this.goal.getConditions());

        if (this.solution != null) {
            this.primaryActionNode = solution.getStartNode();
            this.currentActionNode = this.primaryActionNode;
        }

        return this;
    }

    public void clearGoal(ServerLevel level, E entity, long gameTime) {
        this.stop(level, entity, gameTime);

        this.goal = null;
        this.primaryActionNode = null;
        this.currentActionNode = null;
        this.runningBehaviour = null;
        this.solution = null;
    }

    protected void stop(ServerLevel level, E entity, long gameTime) {
        super.stop(level, entity, gameTime);
    }

    public ActionGraph.OpenedNode<E> getFurthestFulfilledAction(E entity) {
        var node = this.primaryActionNode;

        // Start from beginning if no solution found
        ActionGraph.OpenedNode<E> furthestFulfilledAction = null;

        // FIXME: can be optimized
        while(node != null) {

            if (node.getAction().isAllEffectsFulfilled(entity)) {
                furthestFulfilledAction = node;
            }

            node = node.getParent();
        }

        return furthestFulfilledAction;
    }

    public ActionGraph.OpenedNode<E> getNextActionNode(ServerLevel serverLevel, E entity, long gameTime) {
        var furthestFulfilledAction = this.getFurthestFulfilledAction(entity);
        var node = furthestFulfilledAction != null ? furthestFulfilledAction.getParent() : this.primaryActionNode;

        if (node != null) {
            // FIXME: possibly redundant
            while ( node.getParent() != null && node.getAction().isAllEffectsFulfilled(entity) && ((ExtendedBehaviour<E>) node.getAction()).tryStart(serverLevel, entity, gameTime) ) {
                node =  node.getParent();
            }

            return node;
        } else {
            return null;
        }
    }

    @Override
    protected void tick(ServerLevel level, E owner, long gameTime) {
        super.tick(level, owner, gameTime);
    }

    @Override
    protected @Nullable ExtendedBehaviour<? super E> pickBehaviour(ServerLevel serverLevel, E e, long l, SBLShufflingList<ExtendedBehaviour<? super E>> sblShufflingList) {
        var nextActionNode = getNextActionNode(serverLevel, e, l);

        if (nextActionNode != null) {
            var hasCycled = this.currentActionNode != null && this.currentActionNode == this.solution.getEndNode() && nextActionNode == this.solution.getStartNode();

            if(hasCycled && !this.goal.isPersistent()) {
                this.clearGoal(serverLevel, e, l);

                return null;
            }

            this.currentActionNode = nextActionNode;
            var nextAction = (ExtendedBehaviour<E>) nextActionNode.getAction();

            return nextAction != null && nextAction.tryStart(serverLevel, e, l) ? nextAction : null;
        }

        return null;
    }

    @Override
    protected boolean doStartCheck(ServerLevel level, E entity, long gameTime) {
        if (!super.doStartCheck(level, entity, gameTime)) {
            return false;
        } else {
            return (this.runningBehaviour = this.pickBehaviour(level, entity, gameTime, this.behaviours)) != null;
        }
    }
}
