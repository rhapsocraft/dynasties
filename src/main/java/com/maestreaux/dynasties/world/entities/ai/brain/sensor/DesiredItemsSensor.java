package com.maestreaux.dynasties.world.entities.ai.brain.sensor;

import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.init.ModSensorTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;

import java.util.List;

public class DesiredItemsSensor<E extends AbstractDynastyVillager> extends ExtendedSensor<E> {
    private static final List<MemoryModuleType<?>> MEMORIES;

    protected void doTick(ServerLevel level, E entity) {
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return ModSensorTypes.FULLY_GROWN_CROPS.get();
    }

    static {
        MEMORIES = ObjectArrayList.of(ModMemoryTypes.FULLY_GROWN_CROPS.get());
    }
}
