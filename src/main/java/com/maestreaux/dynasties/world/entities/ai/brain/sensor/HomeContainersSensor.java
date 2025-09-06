package com.maestreaux.dynasties.world.entities.ai.brain.sensor;

import com.maestreaux.dynasties.core.utils.AIUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.init.ModSensorTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Comparator;
import java.util.List;


public class HomeContainersSensor<E extends AbstractDynastyVillager> extends ExtendedSensor<E> {
    private static final List<MemoryModuleType<?>> MEMORIES;

    protected void doTick(ServerLevel level, E entity) {
        var homePlot = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_PLOT.get());

        if (homePlot != null) {
            // TODO: HEIGHT TEMPORARY AND MAKE VALID CONTAINERS CONFIGURABLE
            var inventories = AIUtils.getPlotContainersStream(homePlot, level).sorted(Comparator.comparingDouble(inventory -> entity.distanceToSqr(inventory.getBlockPos().getCenter()))).toList();;

            if (inventories.isEmpty()) {
                BrainUtil.clearMemory(entity, ModMemoryTypes.HOME_CONTAINERS.get());
            } else {
                BrainUtil.setMemory(entity, ModMemoryTypes.HOME_CONTAINERS.get(), inventories);
            }
        }
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return ModSensorTypes.HOME_CONTAINERS.get();
    }

    static {
        MEMORIES = ObjectArrayList.of(ModMemoryTypes.HOME_CONTAINERS.get());
    }
}
