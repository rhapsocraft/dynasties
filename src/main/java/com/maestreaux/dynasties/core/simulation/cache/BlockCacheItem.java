package com.maestreaux.dynasties.core.simulation.cache;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockCacheItem  implements IRandomTickable {
    protected BlockState state;
    protected ZoneCache cache;
    protected BlockPos pos;
    protected ServerLevel level;
    protected int blockLightLevel;
    protected boolean skyLightInColumn;


    public BlockCacheItem(ZoneCache cache, BlockPos pos) {
        this.cache = cache;
        this.level = cache.level();
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
        this.blockLightLevel = this.level.getBrightness(LightLayer.BLOCK, this.pos);
        this.skyLightInColumn = this.level.getLightEngine().lightOnInColumn(this.pos.asLong());
    }

    public int getLightLevel() {
        var skyLight = 15 - this.level.getSkyDarken();

        // Get maximum value of cached block light value vs global skylight value
        return Math.max(this.blockLightLevel, this.skyLightInColumn ? skyLight : 0);
    }

    public void applyBlockState() {
        this.level.setBlock(this.pos, this.state, Block.UPDATE_CLIENTS);
    }

    @Override
    public void randomTick() {

    }
}
