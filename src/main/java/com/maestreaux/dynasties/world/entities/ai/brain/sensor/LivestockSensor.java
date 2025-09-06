package com.maestreaux.dynasties.world.entities.ai.brain.sensor;

import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.init.ModSensorTypes;
import com.maestreaux.dynasties.world.Partition;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Comparator;
import java.util.List;

public class LivestockSensor<E extends AbstractDynastyVillager> extends ExtendedSensor<E> {
    private static final List<MemoryModuleType<?>> MEMORIES;

    @Override
    protected void doTick(ServerLevel level, E entity) {
        List<LivingEntity> livestock = new ObjectArrayList<>();
        var homePlot = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_PLOT.get());

        if (homePlot != null) {
            var partitions = homePlot.getPartitionsByType(Partition.PartitionType.RANCH);

            for (var partition : partitions) {
                var origin = Vec3.atLowerCornerOf(partition.getAbsoluteOrigin());
                var livestockInPartition = level.getEntities(EntityTypeTest.forClass(LivingEntity.class),
                        new AABB(origin, new Vec3(origin.x + partition.getWidth(), origin.y + 5, origin.z + partition.getLength())),
                        (animal) -> animal instanceof Pig
                );

                livestock.addAll(livestockInPartition);
            }

            if (livestock.isEmpty()) {
                BrainUtil.clearMemory(entity, ModMemoryTypes.HOME_LIVESTOCK.get());
            } else {
                livestock.sort(Comparator.comparingDouble(animal -> entity.distanceToSqr((animal.position()))));
                BrainUtil.setMemory(entity, ModMemoryTypes.HOME_LIVESTOCK.get(), livestock);
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
        return ModSensorTypes.HOME_LIVESTOCK.get();
    }

    static {
        MEMORIES = ObjectArrayList.of(ModMemoryTypes.HOME_LIVESTOCK.get(), ModMemoryTypes.HOME_PLOT.get());
    }
}
