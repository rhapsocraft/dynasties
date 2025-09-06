package com.maestreaux.dynasties.core.simulation.cache;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;

public class CropCacheItem extends BlockCacheItem {

    public CropCacheItem(ServerLevel level, BlockPos pos) {
        super(level, pos);
    }

    protected float getGrowthSpeed() {
        return 1F;
    }

    @Override
    public void randomTick() {
        if (this.lightLevel >= 9) {
            var cropBlock = (CropBlock) this.state.getBlock();
            var age = cropBlock.getAge(this.state);
            var maxAge = cropBlock.getMaxAge();

            if (age < maxAge) {

            }
        }
    }
}
