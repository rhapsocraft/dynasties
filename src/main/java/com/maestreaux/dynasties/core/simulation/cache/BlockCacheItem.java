package com.maestreaux.dynasties.core.simulation.cache;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;


public class BlockCacheItem implements IRandomTickable {
    protected BlockState state;
    protected ZoneCache cache;
    protected BlockPos pos;
    protected ServerLevel level;
    protected int blockLightLevel;
    protected boolean canSeeSky;
    protected CacheStatus status = CacheStatus.FLUSHED;

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
        // Cache block light level
        this.blockLightLevel = this.level.getBrightness(LightLayer.BLOCK, this.pos);

        // Cache direct sky access
        this.canSeeSky = this.level.canSeeSky(this.pos);
    }

    public int getLightLevel() {
        var skyLight = 15 - this.level.getSkyDarken();

        // Get maximum value of cached block light value vs global skylight value
        return Math.max(this.blockLightLevel, this.canSeeSky ? skyLight : 0);
    }

    public void cacheBlockState() {
        this.updateLightLevel();
        this.updateBlockState();
        this.setStatus(BlockCacheItem.CacheStatus.CACHED);
    }

    public void applyBlockState() {
        this.level.setBlock(this.pos, this.state, Block.UPDATE_CLIENTS);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public CacheStatus getStatus() {
        return this.status;
    }

    public void setStatus(CacheStatus newStatus) {
        this.status = newStatus;
    }

    @Override
    public void randomTick() {

    }

    public enum CacheStatus {
        CACHED,
        PENDING,
        FLUSHED
    }
}
