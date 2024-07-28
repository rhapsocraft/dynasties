package com.maestreaux.dynasties.init;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.entities.ai.brain.sensor.AvailablePlotsSensor;
import com.maestreaux.dynasties.world.entities.ai.brain.sensor.AvailableTentsSensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSensorTypes {
    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(ForgeRegistries.Keys.SENSOR_TYPES, DynastiesMod.MODID);
    public static final RegistryObject<SensorType<AvailablePlotsSensor<?>>> AVAILABLE_PLOTS = SENSOR_TYPES.register("available_plots", () -> new SensorType<>(AvailablePlotsSensor::new));
    public static final RegistryObject<SensorType<AvailableTentsSensor<?>>> AVAILABLE_TENTS = SENSOR_TYPES.register("available_tents", () -> new SensorType<>(AvailableTentsSensor::new));

}
