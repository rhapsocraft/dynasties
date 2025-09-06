package com.maestreaux.dynasties.core.production.asset;

import com.maestreaux.dynasties.core.production.Production;
import com.maestreaux.dynasties.core.utils.AIUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.maestreaux.dynasties.world.entities.blockentity.WorkstationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.util.BrainUtil;

public class BlockEntityWorkstationAssetAccessor<B extends WorkstationBlockEntity> implements Production.IAssetAccessor<BlockEntityAsset<B>> {
    private final Class<B> assetClass;

    public BlockEntityWorkstationAssetAccessor(Class<B> blockEntityClass) {
        this.assetClass = blockEntityClass;
    }

    @Override
    public BlockEntityAsset<B> lookup(AbstractDynastyVillager villager) {
        var homePlot = BrainUtil.getMemory(villager, ModMemoryTypes.HOME_PLOT.get());

        if (homePlot != null) {
            var asset = BlockPos.betweenClosedStream(homePlot.getAbsoluteStartPos(), homePlot.getAbsoluteEndPos().offset(0, 10, 0))
                    .map((pos) -> {
                        var blockEntity = villager.level().getBlockEntity(pos);
                        return this.assetClass.isInstance(blockEntity) ? blockEntity: null;
                    }).filter(assetClass::isInstance).findFirst().orElse(null);

            if (asset != null) {
                return new BlockEntityAsset<>(this.assetClass.cast(asset));
            }
        }

        return null;
    }

    @Override
    public boolean canUse(BlockEntityAsset<B> asset, AbstractDynastyVillager villager) {
        var closeEnough =  AIUtils.isCloseEnoughToTarget(villager, asset.get().getBlockPos());

        if (closeEnough) {
            BrainUtil.clearMemory(villager, MemoryModuleType.LOOK_TARGET);
            BrainUtil.clearMemory(villager, MemoryModuleType.WALK_TARGET);
        }

        return closeEnough;
    }

    @Override
    public void attemptUse(BlockEntityAsset<B> asset, AbstractDynastyVillager villager) {
        var targetPos = asset.get().getBlockPos();

        BrainUtil.setMemory(villager, MemoryModuleType.LOOK_TARGET, new BlockPosTracker(targetPos));
        BrainUtil.setMemory(villager, MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, 0.6F, 1));
    }

    @Override
    public void work(BlockEntityAsset<B> asset, Production<?,?,?> production, AbstractDynastyVillager villager) {
        asset.get().work(production, villager);
    }

    @Override
    public boolean canContinueWork(BlockEntityAsset<B> asset, AbstractDynastyVillager villager) {
        var workstation = asset.get();
        return workstation.started() && !workstation.completed();
    }

    @Override
    public boolean isCompleted(BlockEntityAsset<B> asset, AbstractDynastyVillager villager) {
        return asset.get().completed();
    }

    @Override
    public void setProduction(BlockEntityAsset<B> asset, Production<?, ?, ?> production, AbstractDynastyVillager villager) {
        asset.get().setProduction(production, villager);
    }
}
