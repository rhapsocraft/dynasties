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
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Comparator;
import java.util.List;

public class FarmlandsSensor<E extends AbstractDynastyVillager> extends ExtendedSensor<E> {
    private static final List<MemoryModuleType<?>> MEMORIES;

    protected void doTick(ServerLevel level, E entity) {
        List<BlockPos> farmlands = new ObjectArrayList();
        var homePlot = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_PLOT.get());

        if (homePlot != null) {
            var partitions = homePlot.getPartitionsByType(Partition.PartitionType.GARDEN);

            for (var partition: partitions) {
                var farmlandsIter = BlockPos.betweenClosed(partition.getAbsoluteOrigin(), partition.getAbsoluteOrigin().offset(partition.getWidth(), 10, partition.getLength())).iterator();

                while(farmlandsIter.hasNext()) {
                    BlockPos pos = farmlandsIter.next();
                    BlockState state = level.getBlockState(pos);

                    var isValid = level.getBlockState(pos.above()).isAir();

                    if (state.getBlock() instanceof FarmBlock && isValid) {
                        farmlands.add(pos.immutable());
                    }
                }
            }

            if (farmlands.isEmpty()) {
                BrainUtil.clearMemory(entity, ModMemoryTypes.HOME_FARMLANDS.get());
            } else {
                farmlands.sort(Comparator.comparingDouble(farmland -> entity.distanceToSqr(farmland.getCenter())));
                BrainUtil.setMemory(entity, ModMemoryTypes.HOME_FARMLANDS.get(), farmlands);
            }
        } else {
            BrainUtil.clearMemory(entity, ModMemoryTypes.HOME_FARMLANDS.get());
        }
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return ModSensorTypes.HOME_FARMLANDS.get();
    }

    static {
        MEMORIES = ObjectArrayList.of(ModMemoryTypes.HOME_FARMLANDS.get(), ModMemoryTypes.HOME_PLOT.get());
    }
}
