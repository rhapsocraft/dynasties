package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.production.Production;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

public class DoProduction<E extends AbstractDynastyVillager> extends DelayedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;

    private Production<?, ?, ?> production;
    private Production.IAsset<?> asset;
    private Production.IAssetAccessor<?> assetAccessor;

    public DoProduction() {
        // 32 ticks serve as a single unit of work
        super(32);
    }

    @Override
    protected void doDelayedAction(E entity) {
        doWorkOnAsset(this.assetAccessor, entity);
        // entity.stopPerformingWork();

    }


    @SuppressWarnings("unchecked")
    private <T extends Production.IAsset<?>> boolean attemptUseAsset(Production.IAssetAccessor<T> accessor, AbstractDynastyVillager entity) {
        T asset = (T) this.asset;

        if (asset != null) {
            if (!accessor.canUse(asset, entity)) {
                accessor.attemptUse(asset, entity);
                return false;
            } else {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private <T extends Production.IAsset<?>> void doWorkOnAsset(Production.IAssetAccessor<T> accessor, AbstractDynastyVillager entity) {
        T asset = (T) this.asset;

        accessor.work(asset, this.production, entity);
    }

    private <T extends Production.IAsset<?>> T getAsset(Production.IAssetAccessor<T> accessor, AbstractDynastyVillager entity) {
        return accessor.lookup(entity);
    }

    @SuppressWarnings("unchecked")
    private <T extends Production.IAsset<?>> boolean canContinueWork(Production.IAssetAccessor<T> accessor, AbstractDynastyVillager entity) {
        T asset = (T) this.asset;

        return accessor.canContinueWork(asset, entity);
    }

    @SuppressWarnings("unchecked")
    private <T extends Production.IAsset<?>> boolean isCompleted(Production.IAssetAccessor<T> accessor, AbstractDynastyVillager entity) {
        T asset = (T) this.asset;

        return accessor.isCompleted(asset, entity);
    }

    protected void start(E entity) {
        // entity.startPerformingWork();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        // Get Available Assets
        var newProduction = BrainUtil.getMemory(entity, ModMemoryTypes.BEST_PRODUCTION_TASK.get());

        if (this.production != newProduction) {
            this.assetAccessor = null;
            this.asset = null;

            this.production = newProduction;
        }

        if (this.production != null) {
            if (this.assetAccessor == null || this.asset == null) {
                var assetAccessor = production.getAsset();

                if (assetAccessor != null) {
                    this.assetAccessor = assetAccessor;

                    this.asset = getAsset(this.assetAccessor, entity);
                }
            }

            if (this.production.canProduce(entity) || (this.assetAccessor != null && this.asset != null && canContinueWork(this.assetAccessor, entity))) {
                return attemptUseAsset(this.assetAccessor, entity);
            }
        }

        return false;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {

        // Best Production
        return MEMORY_REQUIREMENTS;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{
                Pair.of(ModMemoryTypes.BEST_PRODUCTION_TASK.get(), MemoryStatus.VALUE_PRESENT),
        });
    }
}
