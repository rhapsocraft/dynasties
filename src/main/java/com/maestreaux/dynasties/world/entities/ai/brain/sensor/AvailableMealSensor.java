package com.maestreaux.dynasties.world.entities.ai.brain.sensor;

import com.maestreaux.dynasties.core.utils.MealUtils;
import com.maestreaux.dynasties.init.ModMemoryTypes;
import com.maestreaux.dynasties.init.ModSensorTypes;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import com.maestreaux.dynasties.world.entities.blockentity.CampfirePotBlockEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

public class AvailableMealSensor<E extends AbstractDynastyVillager> extends ExtendedSensor<E> {
    private static final List<MemoryModuleType<?>> MEMORIES;

    @Override
    protected void doTick(ServerLevel level, E entity) {
        var pots = BrainUtil.getMemory(entity, ModMemoryTypes.HOME_CAMPFIRE_POTS.get());

        if (pots != null) {
            var validPot = pots.stream().filter(CampfirePotBlockEntity::hasContents).min((pot1, pot2) -> Float.compare(MealUtils.getMealDesirability(entity, pot2.getMeal()), MealUtils.getMealDesirability(entity, pot1.getMeal())))
                    .orElse(null);

            if (validPot != null) {
                BrainUtil.setMemory(entity, ModMemoryTypes.AVAILABLE_MEAL.get(), validPot);
            } else {
                BrainUtil.clearMemory(entity, ModMemoryTypes.AVAILABLE_MEAL.get());
            }
        }
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return ModSensorTypes.AVAILABLE_MEAL.get();
    }

    static {
        MEMORIES = ObjectArrayList.of(ModMemoryTypes.AVAILABLE_MEAL.get(), ModMemoryTypes.HOME_CAMPFIRE_POTS.get());
    }
}
