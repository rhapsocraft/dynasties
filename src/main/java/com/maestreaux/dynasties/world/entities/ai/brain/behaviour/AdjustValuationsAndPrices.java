package com.maestreaux.dynasties.world.entities.ai.brain.behaviour;

import com.maestreaux.dynasties.core.utils.InventoryUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

public class AdjustValuationsAndPrices<E extends AbstractDynastyVillager> extends ExtendedBehaviour<E> {
    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS;

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    protected void start(E entity) {
        var containers = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_CONTAINERS.get());
        var marketAgent = entity.asMarketAgent();

        var valuations = marketAgent.getValuations();

        for(var entry : valuations.entrySet()) {
            var item = entry.getKey();
            var valuation = entry.getValue();

            var desiredSupply = entity.asMarketAgent().getDesiredSupply(item);
            var currentSupply = InventoryUtils.getItemSupply(containers, item);
            var surplusPercentage = ((float) desiredSupply / currentSupply);
            var weightedAverage = 0.5F * surplusPercentage + 0.5F;

            valuations.put(item, valuation * weightedAverage);
        }

        marketAgent.valuationsLastUpdated = entity.level().getGameTime();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return level.getGameTime() - entity.asMarketAgent().valuationsLastUpdated > 12_000;
    }

    static {
        MEMORY_REQUIREMENTS = ObjectArrayList.of(new Pair[]{
                Pair.of(ModMemoryTypes.HOME_CONTAINERS.get(), MemoryStatus.VALUE_PRESENT),
        });
    }
}
