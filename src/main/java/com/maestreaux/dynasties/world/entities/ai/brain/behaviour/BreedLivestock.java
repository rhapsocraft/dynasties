package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.utils.AIUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.animal.Animal;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

public class BreedLivestock<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;
    private Animal targetEntity;

    // TEMPORARY CONSTRAINT
    private static int MAX_LIVESTOCK = 12;

    protected void start(E entity) {
        var availableLivestock = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_LIVESTOCK.get());

        if (availableLivestock != null) {
            if (this.targetEntity != null && this.targetEntity.isAlive()) {
                if (AIUtils.isCloseEnoughToTarget(entity, this.targetEntity.getOnPos(), 3)) {
                    this.targetEntity.setInLove(null);
                    this.cooldownFor((e) -> 10);

                    this.targetEntity = null;
                    BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
                    BrainUtil.clearMemory(entity, MemoryModuleType.LOOK_TARGET);
                } else {
                    BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetEntity, 0.6F, 2));
                    BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new EntityTracker(this.targetEntity, true));
                }
            } else {
                Animal animal = (Animal) availableLivestock.stream().filter((le) -> {
                    var _animal = (Animal) le;
                    return _animal.getAge() == 0 && _animal.canFallInLove();
                }).findFirst().orElse(null);

                if (animal != null) {
                    this.targetEntity = animal;
                }
            }
        }
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        var livestock = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_LIVESTOCK.get());

        if (livestock != null) {
            var animalsInLove = livestock.stream().filter(le -> le instanceof Animal animal && animal.getAge() == 0 && (animal.isInLove() || animal.canFallInLove())).count();

            return animalsInLove >= 2 && !livestock.isEmpty() && livestock.size() < MAX_LIVESTOCK;
        } else {
            return false;
        }
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{
                Pair.of(ModMemoryTypes.HOME_PLOT.get(), MemoryStatus.VALUE_PRESENT),
                Pair.of(ModMemoryTypes.HOME_LIVESTOCK.get(), MemoryStatus.VALUE_PRESENT)
        });
    }
}
