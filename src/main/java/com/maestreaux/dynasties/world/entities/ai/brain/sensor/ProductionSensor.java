package com.maestreaux.dynasties.world.entities.ai.brain.sensor;

import com.maestreaux.dynasties.core.production.Production;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.init.ModProductions;
import com.maestreaux.dynasties.init.ModSensorTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.ArrayList;
import java.util.List;

public class ProductionSensor<E extends AbstractDynastyVillager> extends ExtendedSensor<E> {
    private static final List<MemoryModuleType<?>> MEMORIES;

    // TODO: Temporary set of productions to use as MVP
    List<Production<?, ?, ?>> productions = List.of(
            ModProductions.WOOL_YARN
    );

    @Override
    protected void doTick(ServerLevel level, E entity) {
            // Get Villager Productions
            // Get available production tickets per house. This is so household members will not produce the same thing at once
//            var bestProduction = this.productions.stream().filter(production ->
//                        // Tickets would represent how many more workers can use perform work on the asset
//                        // homePlot.getTickets(plotProd) > 0
//                        // Determine if we can produce (e.g if we have enough materials or if we have available assets)
//                        && production.canProduce(entity))
//                    // Sort by how much villager values the product
//                    .sorted(plotProd.evaluate(entity)).findFirst().orElse(null);

            var bestProduction = this.productions.stream()
                    .min((p1, p2) -> Float.compare(p2.evaluate(entity), p1.evaluate(entity))).orElse(null);

            if (bestProduction != null) {
                BrainUtil.setMemory(entity, ModMemoryTypes.BEST_PRODUCTION_TASK.get(), bestProduction);
            } else {
                BrainUtil.clearMemory(entity, ModMemoryTypes.BEST_PRODUCTION_TASK.get());
            }
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return ModSensorTypes.BEST_PRODUCTION_TASK.get();
    }

    static {
        MEMORIES = ObjectArrayList.of(ModMemoryTypes.BEST_PRODUCTION_TASK.get());
    }
}
