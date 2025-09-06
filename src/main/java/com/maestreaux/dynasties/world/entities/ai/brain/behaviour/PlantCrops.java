package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.Dictionaries;
import com.maestreaux.dynasties.core.utils.AIUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

public class PlantCrops<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;
    private BlockPos targetFarmLand;

    protected void start(E entity) {
        var farmlands = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_FARMLANDS.get());

        if (farmlands != null) {
            if (this.targetFarmLand != null) {
                var targetPos = this.targetFarmLand.above();
                if (AIUtils.isCloseEnoughToTarget(entity, targetPos)) {
                    ItemStack itemToPlant = null;

                    var size = entity.getInventory().getContainerSize();
                    for (int i = 0; i < size; i++) {
                        var itemStack = entity.getInventory().getItem(i);

                        if (Dictionaries.VALID_SEEDS.contains(itemStack.getItem())) {
                            itemToPlant = itemStack;
                        };
                    }

                    if (itemToPlant != null) {
                        entity.level().setBlock(targetPos, Block.byItem(itemToPlant.getItem()).defaultBlockState(), 3);
                        itemToPlant.shrink(1);
                    }

                    this.targetFarmLand = null;
                } else {
                    BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, 0.6F, 1));
                    BrainUtil.setMemory(entity, MemoryModuleType.LOOK_TARGET, new BlockPosTracker(targetPos));
                }

            } else {
                this.targetFarmLand = farmlands.stream().filter(farmland -> entity.level().getBlockState(farmland.above()).isAir()).findFirst().orElse(null);
                BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
                BrainUtil.clearMemory(entity, MemoryModuleType.LOOK_TARGET);
            }
        }
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return entity.getInventory().hasAnyOf(Dictionaries.VALID_SEEDS);
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{
                Pair.of(ModMemoryTypes.HOME_FARMLANDS.get(), MemoryStatus.VALUE_PRESENT)
        });
    }
}
