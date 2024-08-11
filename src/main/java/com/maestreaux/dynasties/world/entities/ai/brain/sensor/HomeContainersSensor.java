package com.maestreaux.dynasties.world.entities.ai.brain.sensor;

import com.maestreaux.dynasties.init.ModCapabilities;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.init.ModSensorTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER;


public class HomeContainersSensor<E extends AbstractDynastyVillager> extends ExtendedSensor<E> {
    private static final List<MemoryModuleType<?>> MEMORIES;

    protected void doTick(ServerLevel level, E entity) {
        var homePlot = BrainUtils.getMemory(entity, ModMemoryTypes.HOME_PLOT.get());

        if (homePlot != null) {
            // TODO: HEIGHT TEMPORARY AND MAKE VALID CONTAINERS CONFIGURABLE
            var inventories = BlockPos.betweenClosedStream(homePlot.getAbsoluteStartPos(), homePlot.getAbsoluteEndPos().offset(0, 10, 0)).map(level::getBlockEntity).filter(Objects::nonNull).filter(
                    blockEntity -> blockEntity instanceof BarrelBlockEntity || blockEntity instanceof ChestBlockEntity
            ).sorted(Comparator.comparingDouble(inventory -> entity.distanceToSqr(inventory.getBlockPos().getCenter()))).toList();

            if (inventories.isEmpty()) {
                BrainUtils.clearMemory(entity, ModMemoryTypes.HOME_CONTAINERS.get());
            } else {
                BrainUtils.setMemory(entity, ModMemoryTypes.HOME_CONTAINERS.get(), inventories);
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
