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
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FullyGrownCropsSensor<E extends AbstractDynastyVillager> extends ExtendedSensor<E> {
    private static final List<MemoryModuleType<?>> MEMORIES;

    protected void doTick(ServerLevel level, E entity) {
        var crops = new ArrayList<BlockPos>();
        var homePlot = BrainUtils.getMemory(entity, ModMemoryTypes.HOME_PLOT.get());

        if (homePlot != null) {
            var partitions = homePlot.getPartitionsByType(Partition.PartitionType.GARDEN);

            for (var partition: partitions) {
                var cropsIter = BlockPos.betweenClosed(partition.getAbsoluteOrigin(), partition.getAbsoluteOrigin().offset(partition.getWidth(), 10, partition.getLength())).iterator();

                while(cropsIter.hasNext()) {
                    BlockPos pos = cropsIter.next();
                    BlockState state = level.getBlockState(pos);

                    if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                        crops.add(pos.immutable());
                    }
                }
            }

            if (crops.isEmpty()) {
                BrainUtils.clearMemory(entity, ModMemoryTypes.FULLY_GROWN_CROPS.get());
            } else {
                crops.sort(Comparator.comparingDouble(crop -> entity.distanceToSqr(crop.getCenter())));
                BrainUtils.setMemory(entity, ModMemoryTypes.FULLY_GROWN_CROPS.get(), crops);
            }
        }
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