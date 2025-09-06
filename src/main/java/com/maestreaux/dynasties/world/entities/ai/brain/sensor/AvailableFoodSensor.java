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
import net.minecraft.world.item.Items;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;
import java.util.Set;

public class AvailableFoodSensor<E extends AbstractDynastyVillager> extends ExtendedSensor<E> {
    private static final List<MemoryModuleType<?>> MEMORIES;

    @Override
    protected void doTick(ServerLevel level, E entity) {
        var containers = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_CONTAINERS.get());

        if (containers != null) {
            var foodLocations = InventoryUtils.getItemLocations(containers, Dictionaries.FOOD).values().stream().flatMap(List::stream).sorted(InventoryUtils.itemLocationNutritionSorter).toList();

            if (foodLocations.isEmpty()) {
                BrainUtil.clearMemory(entity, ModMemoryTypes.AVAILABLE_FOOD.get());
            } else {
                BrainUtil.setMemory(entity, ModMemoryTypes.AVAILABLE_FOOD.get(), foodLocations);
            }
        }
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return ModSensorTypes.AVAILABLE_FOOD.get();
    }

    static {
        MEMORIES = ObjectArrayList.of(ModMemoryTypes.AVAILABLE_FOOD.get(), ModMemoryTypes.HOME_CONTAINERS.get());
    }
}
