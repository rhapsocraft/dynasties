package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.utils.AIUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.common.Mod;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

public class HarvestCrops<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;
    private BlockPos targetCrop;

    protected void start(E entity) {
        var readyCrops = BrainUtil.getMemory(entity, ModMemoryTypes.FULLY_GROWN_CROPS.get());

        if (readyCrops != null && !readyCrops.isEmpty()) {
            if (this.targetCrop != null) {
                Level level = entity.level();
                BlockState blockState = level.getBlockState(this.targetCrop);

                // Double check if max age
                if (AIUtils.isCloseEnoughToTarget(entity, this.targetCrop)) {
                    BlockEntity blockentity = blockState.hasBlockEntity() ? level.getBlockEntity(this.targetCrop) : null;

                    Block.getDrops(blockState, (ServerLevel) level, this.targetCrop, blockentity, entity, ItemStack.EMPTY).forEach(itemStack -> {
                        ItemStack itemsToDrop = entity.getInventory().addItem(itemStack);

                        if (itemsToDrop != ItemStack.EMPTY) {
                            Block.popResource(level, this.targetCrop, itemsToDrop);
                        }
                    });

                    entity.level().destroyBlock(this.targetCrop, false, entity);
                    this.targetCrop = null;
                } else {
                    BrainUtil.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetCrop, 0.6F, 1));
                }
            } else {
                var cropToGet = readyCrops.get(0);
                var cropBlockState = entity.level().getBlockState(cropToGet);

                if (cropBlockState.getBlock() instanceof CropBlock crop && crop.isMaxAge(cropBlockState)) {
                    this.targetCrop = readyCrops.get(0);
                }

                BrainUtil.clearMemory(entity, MemoryModuleType.WALK_TARGET);
            }
        }
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{
                Pair.of(ModMemoryTypes.FULLY_GROWN_CROPS.get(), MemoryStatus.VALUE_PRESENT)
        });
    }
}
