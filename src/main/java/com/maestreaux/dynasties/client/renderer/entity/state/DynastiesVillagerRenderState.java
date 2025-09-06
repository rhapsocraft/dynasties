package com.maestreaux.dynasties.client.renderer.entity.state;

import com.maestreaux.dynasties.core.simulation.entity.VillagerEntitySimulated;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class DynastiesVillagerRenderState extends ArmedEntityRenderState {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState fleeAnimationState = new AnimationState();
    public final AnimationState idleFaceAnimationState = new AnimationState();
    public final AnimationState fallAnimationState = new AnimationState();
    public final AnimationState bounceAnimationState = new AnimationState();
    public final AnimationState turnRightAnimationState = new AnimationState();
    public final AnimationState turnLeftAnimationState = new AnimationState();
    public final AnimationState swingAnimationState = new AnimationState();

    // EATING
    public final AnimationState eatAnimationState = new AnimationState();

    public boolean hasItem;
    public boolean isFleeing;
    public boolean isEating;
    public boolean isFalling;

    public boolean showCrossedArms = true;
    public boolean doHeadRotation = true;
    public boolean isHoldingBlock = false;
    public VillagerEntitySimulated.DebugData debugData = null;

    // TODO: TEMPORARY NOBILITY FLAG
    public boolean isNobility = false;

    public DynastiesVillagerRenderState() {}

}
