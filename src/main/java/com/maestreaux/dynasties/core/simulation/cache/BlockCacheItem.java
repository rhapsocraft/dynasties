package com.maestreaux.dynasties.core.simulation.cache;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class BlockCacheItem  implements IRandomTickable {
    protected BlockState state;
    protected BlockPos pos;
    protected ServerLevel level;
    protected int lightLevel;

    public BlockCacheItem(ServerLevel level, BlockPos pos) {
        this.level = level;
        this.pos = pos;

        updateBlockState();
        updateLightLevel();
    }

    public BlockState getState() {
        return this.state;
    }

    public void updateBlockState() {
        this.state = this.level.getBlockState(this.pos);
    }

    public void updateLightLevel() {
        this.lightLevel = this.level.getRawBrightness(this.pos, 0);
    }

    public void applyBlockState() {
        this.level.setBlock(this.pos, this.state, 3);
    }

    @Override
    public void randomTick() {

    }
}
