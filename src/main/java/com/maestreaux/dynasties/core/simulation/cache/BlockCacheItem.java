package com.maestreaux.dynasties.core.simulation.cache;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;


public class BlockCacheItem implements IRandomTickable, ICacheItem {
    protected BlockState state;
    protected BlockGetter blockGetter;
    protected BlockPos pos;
    protected ServerLevel level;
    protected int blockLightLevel;
    protected boolean canSeeSky;
    protected CacheStatus status = CacheStatus.FLUSHED;

    public BlockCacheItem(BlockGetter blockGetter, ServerLevel level, BlockPos pos) {
        this.blockGetter = blockGetter;
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

    public boolean applyBlockState() {
        return this.level.setBlock(this.pos, this.state, Block.UPDATE_CLIENTS);
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

    public BlockCacheItem collect() {
        this.updateLightLevel();
        this.updateBlockState();

        return this;
    }

    @Override
    public void randomTick() { }

    @Override
    public boolean flush() {
        return this.applyBlockState();
    }

    @Override
    public boolean cache() {
        this.collect();
        this.setStatus(BlockCacheItem.CacheStatus.CACHED);

        return true;
    }

    @Override
    public boolean isLoaded() {
        return this.level.hasChunk(this.pos.getX(), this.pos.getZ());
    }

    public enum CacheStatus {
        CACHED,
        PENDING,
        FLUSHED
    }
}
