package com.maestreaux.dynasties.world.entities.ai.brain.sensor;

import com.maestreaux.dynasties.core.Dictionaries;
import com.maestreaux.dynasties.core.utils.InventoryUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.init.ModSensorTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

public class IngredientsSensor<E extends AbstractDynastyVillager> extends ExtendedSensor<E> {
    private static final List<MemoryModuleType<?>> MEMORIES;

    @Override
    protected void doTick(ServerLevel level, E entity) {
        var containers = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_CONTAINERS.get());

        if (containers != null) {
            var ingredientLocations = InventoryUtils.getItemLocations(containers, Dictionaries.INGREDIENTS).values().stream()
                    .flatMap(List::stream)
                    .sorted(InventoryUtils.itemLocationPotentialNutritionSorter((ServerLevel) entity.level()))
                    .toList();

            if (ingredientLocations.isEmpty()) {
                BrainUtil.clearMemory(entity, ModMemoryTypes.INGREDIENTS.get());
            } else {
                BrainUtil.setMemory(entity, ModMemoryTypes.INGREDIENTS.get(), ingredientLocations);
            }
        }
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return ModSensorTypes.INGREDIENTS.get();
    }

    static {
        MEMORIES = ObjectArrayList.of(ModMemoryTypes.INGREDIENTS.get(), ModMemoryTypes.HOME_CONTAINERS.get());
    }
}
