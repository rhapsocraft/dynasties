package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.InteractWithDoor;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

// TODO: Enable opening of fence blocks by creating new custom navigation for villager
public class InteractWithBarrier<E extends LivingEntity> extends InteractWithDoor<E> {
    @Override
    protected void checkAndCloseDoors(ServerLevel level, E entity, Set<GlobalPos> doorsToClose, BlockPos prevNodePos, BlockPos nextNodePos) {
        Iterator<GlobalPos> iterator = doorsToClose.iterator();

        while(iterator.hasNext()) {
            GlobalPos doorLocation = iterator.next();
            BlockPos doorPos = doorLocation.pos();
            if (!doorPos.equals(prevNodePos) && !doorPos.equals(nextNodePos)) {
                if (doorLocation.dimension() == level.dimension() && doorPos.closerToCenterThan(entity.position(), (double)3.0F)) {
                    BlockState barrierState = level.getBlockState(doorPos);
                    if (this.isInteractableDoor(barrierState)) {
                        var barrierBlock = barrierState.getBlock();

                        if (!this.shouldHoldDoorOpenForOthers(entity, doorPos, BrainUtil.memoryOrDefault(entity, MemoryModuleType.NEAREST_LIVING_ENTITIES, List::of))) {
                            if (barrierBlock instanceof DoorBlock doorBlock && doorBlock.isOpen(barrierState)) {
                                doorBlock.setOpen(entity, level, barrierState, doorPos, false);
                            } /* else if (barrierBlock instanceof FenceGateBlock) {
                                barrierState.setValue(FenceGateBlock.OPEN, false);
                            } */
                        }
                    }

                    iterator.remove();
                } else {
                    iterator.remove();
                }
            }
        }

    }

    @Override
    protected boolean isInteractableDoor(BlockState state) {
        return super.isInteractableDoor(state) /* || state.getBlock() instanceof FenceGateBlock */;
    }

    private void addDoorsToClose(ServerLevel level, E entity, BlockPos pos) {
        Set<GlobalPos> doorPositions = BrainUtil.getMemory(entity, MemoryModuleType.DOORS_TO_CLOSE);

        if (doorPositions == null) {
            doorPositions = new ObjectOpenHashSet<>();
        }

        doorPositions.add(new GlobalPos(level.dimension(), pos));
        BrainUtil.setMemory(entity, MemoryModuleType.DOORS_TO_CLOSE, doorPositions);
    }

    @Override
    protected void tryOpenDoor(ServerLevel level, E entity, BlockState blockState, BlockPos pos) {
        if (blockState.getBlock() instanceof DoorBlock door) {
            if (!door.isOpen(blockState)) {
                door.setOpen(entity, level, blockState, pos, true);
                this.addDoorsToClose(level, entity, pos);
            }
        } /* else if (blockState.getBlock() instanceof FenceGateBlock fenceGate){
            var isOpen = blockState.getValue(FenceGateBlock.OPEN);

            if (!isOpen) {
                blockState.setValue(FenceGateBlock.OPEN, true);
                this.addDoorsToClose(level, entity, pos);
            }
        }*/
    }
}
