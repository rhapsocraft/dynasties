package com.maestreaux.dynasties.core.simulation.cache;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.IPlantable;

import java.util.List;

public class CropCacheItem extends BlockCacheItem {
    public CropCacheItem(BlockGetter blockGetter, ServerLevel level, BlockPos pos) {
        super(blockGetter, level, pos);
    }

    protected boolean isHarvested = false;
    protected float getGrowthSpeed() {
        return 1F;
    }

    protected static float getGrowthSpeed(Block p_52273_, BlockGetter p_52274_, BlockPos p_52275_) {
        float f = 1.0F;
        BlockPos blockpos = p_52275_.below();

        for(int i = -1; i <= 1; ++i) {
            for(int j = -1; j <= 1; ++j) {
                float f1 = 0.0F;
                BlockState blockstate = p_52274_.getBlockState(blockpos.offset(i, 0, j));
                if (blockstate.canSustainPlant(p_52274_, blockpos.offset(i, 0, j), Direction.UP, (IPlantable)p_52273_)) {
                    f1 = 1.0F;
                    if (blockstate.isFertile(p_52274_, p_52275_.offset(i, 0, j))) {
                        f1 = 3.0F;
                    }
                }

                if (i != 0 || j != 0) {
                    f1 /= 4.0F;
                }

                f += f1;
            }
        }

        BlockPos blockpos1 = p_52275_.north();
        BlockPos blockpos2 = p_52275_.south();
        BlockPos blockpos3 = p_52275_.west();
        BlockPos blockpos4 = p_52275_.east();
        boolean flag = p_52274_.getBlockState(blockpos3).is(p_52273_) || p_52274_.getBlockState(blockpos4).is(p_52273_);
        boolean flag1 = p_52274_.getBlockState(blockpos1).is(p_52273_) || p_52274_.getBlockState(blockpos2).is(p_52273_);
        if (flag && flag1) {
            f /= 2.0F;
        } else {
            boolean flag2 = p_52274_.getBlockState(blockpos3.north()).is(p_52273_) || p_52274_.getBlockState(blockpos4.north()).is(p_52273_) || p_52274_.getBlockState(blockpos4.south()).is(p_52273_) || p_52274_.getBlockState(blockpos3.south()).is(p_52273_);
            if (flag2) {
                f /= 2.0F;
            }
        }

        return f;
    }

    public List<ItemStack> harvest() {
        var result = Block.getDrops(this.state, this.level, this.pos, null);
        this.state = Blocks.AIR.defaultBlockState();
        this.isHarvested = true;

        return result;
    }

    public boolean isMature() {
        var cropBlock = (CropBlock) this.state.getBlock();
        var age = cropBlock.getAge(this.state);
        var maxAge = cropBlock.getMaxAge();

        return age == maxAge;
    }

    @Override
    public CropCacheItem collect() {
        super.collect();

        return this;
    }

    @Override
    public void randomTick() {
        if (!this.isHarvested) {
            // Replicated behavior
            if (this.getLightLevel() >= 9) {
                var cropBlock = (CropBlock) this.state.getBlock();
                var age = cropBlock.getAge(this.state);
                var maxAge = cropBlock.getMaxAge();

                if (age < maxAge) {
                    float speed = getGrowthSpeed(cropBlock, this.blockGetter, this.pos);
                    if (this.level.random.nextInt((int)(25.0F / speed) + 1) == 0) {
                        this.state = this.state.setValue(BlockStateProperties.AGE_7, age + 1);
                        this.setStatus(CacheStatus.PENDING);
                    }
                }
            }
        }
    }
}
