package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.utils.AIUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

public class DoConstruction<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;
    private BlockPos targetPos = null;
    private boolean isStopped = false;

    protected void start(E entity) {
        var homePlot = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_PLOT.get());

        if (homePlot != null) {
            var partitionToBuildOn = homePlot.getPartitionToBuildOn();

            if (partitionToBuildOn != null && !partitionToBuildOn.isConstructionFinished()) {
                if (this.targetPos != null) {
                    if (AIUtils.isCloseEnoughToTarget(entity, this.targetPos, 4F) && !entity.swinging) {
                        var blockToPlaceInfo = partitionToBuildOn.getBlocks().get(partitionToBuildOn.getConstructionCursor());
                        var zeroPos = partitionToBuildOn.getBuilding().getTemplate().getZeroPositionWithTransform(blockToPlaceInfo.pos().rotate(partitionToBuildOn.getRotation()), Mirror.NONE, partitionToBuildOn.getRotation());
                        var blockPos = zeroPos.offset(partitionToBuildOn.getAbsoluteOrigin());

                        var blockState = blockToPlaceInfo.state().rotate(entity.level(), blockPos, partitionToBuildOn.getRotation());

                        if (entity.level().setBlock(blockPos, blockState, 3)) {
                            entity.swing(InteractionHand.MAIN_HAND);
                            entity.playSound(blockState.getBlock().getSoundType(blockState, entity.level(), blockPos, entity).getPlaceSound());

                            this.cooldownFor((e) -> 15);
                        } else {
                            this.cooldownFor((e) -> 0);
                        }

                        this.targetPos = null;
                        partitionToBuildOn.incrementConstructionCursor();
                    } else {
                        BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.targetPos));
                        BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetPos, 0.6F, 2));
                    }
                } else {
                    var blockToPlaceInfo = partitionToBuildOn.getBlocks().get(partitionToBuildOn.getConstructionCursor());

                    var zeroPos = partitionToBuildOn.getBuilding().getTemplate().getZeroPositionWithTransform(blockToPlaceInfo.pos().rotate(partitionToBuildOn.getRotation()), Mirror.NONE, partitionToBuildOn.getRotation());
                    this.targetPos = zeroPos.offset(partitionToBuildOn.getAbsoluteOrigin());

                    var blockState = blockToPlaceInfo.state().rotate(entity.level(), this.targetPos, partitionToBuildOn.getRotation());

                    var blockItem = BlockItem.BY_BLOCK.get(blockState.getBlock());
                    if (blockItem != null) {
                        entity.setItemInHand(InteractionHand.MAIN_HAND, blockItem.getDefaultInstance());
                    }
                }
            }
        }
    }

    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        var homePlot = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_PLOT.get());

        if (homePlot != null) {
            var partitionToBuildOn = homePlot.getPartitionToBuildOn();

            if (partitionToBuildOn != null) {
                return !partitionToBuildOn.isConstructionFinished();
            }
        }

        if (!this.isStopped) {
            this.isStopped = true;
            BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
            BrainUtil.clearMemory(entity, MemoryModuleType.LOOK_TARGET);
            entity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }

        return false;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{
                Pair.of(ModMemoryTypes.HOME_PLOT.get(), MemoryStatus.VALUE_PRESENT),
        });
    }
}
