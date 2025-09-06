package com.maestreaux.dynasties.world.entities.ai.brain.sensor;

import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.init.ModSensorTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.maestreaux.dynasties.world.entities.blockentity.CampfirePotBlockEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class HomeCookingPotSensor<E extends AbstractDynastyVillager> extends ExtendedSensor<E> {
    private static final List<MemoryModuleType<?>> MEMORIES;

    protected void doTick(ServerLevel level, E entity) {
        var homePlot = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_PLOT.get());

        if (homePlot != null) {
            // TODO: HEIGHT TEMPORARY AND MAKE VALID CONTAINERS CONFIGURABLE
            var cookingPot = BlockPos.betweenClosedStream(homePlot.getAbsoluteStartPos(), homePlot.getAbsoluteEndPos().offset(0, 10, 0)).map((pos) -> {
                var blockEntity = level.getBlockEntity(pos);
                return blockEntity instanceof CampfirePotBlockEntity cookingPotBlockEntity ? cookingPotBlockEntity : null;
            }).filter(Objects::nonNull).sorted(Comparator.comparingDouble(pot -> entity.distanceToSqr(pot.getBlockPos().getCenter()))).toList();

            if (cookingPot.isEmpty()) {
                BrainUtil.clearMemory(entity, ModMemoryTypes.HOME_CAMPFIRE_POTS.get());
            } else {
                BrainUtil.setMemory(entity, ModMemoryTypes.HOME_CAMPFIRE_POTS.get(), cookingPot);
            }
        }
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return ModSensorTypes.HOME_CAMPFIRE_POT.get();
    }

    static {
        MEMORIES = ObjectArrayList.of(ModMemoryTypes.HOME_CAMPFIRE_POTS.get());
    }
}
