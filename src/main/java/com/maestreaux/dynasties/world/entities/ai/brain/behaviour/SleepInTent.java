package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import javax.annotation.Nullable;
import java.util.List;

public class SleepInTent<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;
    @Nullable
    private BlockPos targetPos = null;
    protected void start(E entity) {
        this.targetPos = BrainUtils.getMemory(entity, ModMemoryTypes.AVAILABLE_TENT.get());

        if (this.targetPos != null) {

            if (isCloseEnoughToTarget(entity)) {
                entity.startSleeping(this.targetPos);
                this.targetPos = null;
            } else {
                BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetPos, 1F, 0));
            }

        } else {
            BrainUtils.clearMemory(entity, MemoryModuleType.WALK_TARGET);
        }
    }

    public boolean isCloseEnoughToTarget(E entity) {
        return this.targetPos != null && entity.blockPosition().distSqr(this.targetPos) <= 1F;
    }

    @Override
    protected void tick(ServerLevel level, E entity, long gameTime) {
        super.tick(entity);

        if (this.targetPos != null && isCloseEnoughToTarget(entity)) {
            entity.startSleeping(this.targetPos);
            this.targetPos = null;
        }
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{
                Pair.of(ModMemoryTypes.AVAILABLE_TENT.get(), MemoryStatus.VALUE_PRESENT),
        });
    }
}
