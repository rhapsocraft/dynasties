package com.maestreaux.dynasties.init;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.entities.ai.brain.sensor.*;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSensorTypes {
    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(ForgeRegistries.Keys.SENSOR_TYPES, DynastiesMod.MODID);
    public static final RegistryObject<SensorType<AvailablePlotsSensor<?>>> AVAILABLE_PLOTS = SENSOR_TYPES.register("available_plots", () -> new SensorType<>(AvailablePlotsSensor::new));
    public static final RegistryObject<SensorType<AvailableTentsSensor<?>>> AVAILABLE_TENTS = SENSOR_TYPES.register("available_tents", () -> new SensorType<>(AvailableTentsSensor::new));
    public static final RegistryObject<SensorType<FarmlandsSensor<?>>> FARMLANDS = SENSOR_TYPES.register("farmlands", () -> new SensorType<>(FarmlandsSensor::new));
    public static final RegistryObject<SensorType<HomeContainersSensor<?>>> HOME_CONTAINERS = SENSOR_TYPES.register("home_containers", () -> new SensorType<>(HomeContainersSensor::new));
    public static final RegistryObject<SensorType<AvailableSeedsSensor<?>>> AVAILABLE_SEEDS = SENSOR_TYPES.register("home_storages", () -> new SensorType<>(AvailableSeedsSensor::new));
    public static final RegistryObject<SensorType<FullyGrownCropsSensor<?>>> FULLY_GROWN_CROPS = SENSOR_TYPES.register("fully_grown_crops", () -> new SensorType<>(FullyGrownCropsSensor::new));
    public static final RegistryObject<SensorType<AvailableFoodSensor<?>>> AVAILABLE_FOOD = SENSOR_TYPES.register("available_food", () -> new SensorType<>(AvailableFoodSensor::new));
}
