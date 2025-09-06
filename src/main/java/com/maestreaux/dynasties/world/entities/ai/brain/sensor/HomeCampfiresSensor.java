package com.maestreaux.dynasties.world.entities.ai.brain.sensor;

import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.init.ModSensorTypes;
import com.maestreaux.dynasties.world.Partition;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class HomeCampfiresSensor<E extends AbstractDynastyVillager> extends ExtendedSensor<E> {
    private static final List<MemoryModuleType<?>> MEMORIES;

    protected void doTick(ServerLevel level, E entity) {
        var homePlot = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_PLOT.get());

        if (homePlot != null) {
            // TODO: HEIGHT TEMPORARY AND MAKE VALID CONTAINERS CONFIGURABLE
            var campfires = BlockPos.betweenClosedStream(homePlot.getAbsoluteStartPos(), homePlot.getAbsoluteEndPos().offset(0, 10, 0)).map((pos) -> {
                var blockEntity = level.getBlockEntity(pos);
                return blockEntity instanceof CampfireBlockEntity campfireBlockEntity ? campfireBlockEntity : null;
            }).filter(Objects::nonNull).sorted(Comparator.comparingDouble(campfire -> entity.distanceToSqr(campfire.getBlockPos().getCenter()))).toList();

            if (campfires.isEmpty()) {
                BrainUtil.clearMemory(entity, ModMemoryTypes.HOME_CAMPFIRES.get());
            } else {
                BrainUtil.setMemory(entity, ModMemoryTypes.HOME_CAMPFIRES.get(), campfires);
            }
        }
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return ModSensorTypes.HOME_CAMPFIRES.get();
    }

    static {
        MEMORIES = ObjectArrayList.of(ModMemoryTypes.HOME_CAMPFIRES.get());
    }
}
