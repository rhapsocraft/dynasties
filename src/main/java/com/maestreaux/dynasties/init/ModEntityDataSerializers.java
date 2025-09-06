package com.maestreaux.dynasties.init;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.core.MarketAgent;
import com.maestreaux.dynasties.core.simulation.entity.VillagerEntitySimulated;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class ModEntityDataSerializers {
    public static DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, DynastiesMod.MODID);
    
    public static final RegistryObject<EntityDataSerializer<List<MarketAgent.TradeOffer>>> TRADE_OFFERS = ENTITY_DATA_SERIALIZERS.register("trade_offers", () ->
            EntityDataSerializer.forValueType(MarketAgent.TradeOffer.STREAM_CODEC.apply(ByteBufCodecs.list())));

    public static final RegistryObject<EntityDataSerializer<VillagerEntitySimulated.DebugData>> VILLAGER_DEBUG_DATA = ENTITY_DATA_SERIALIZERS.register("villager_debug_data", () -> EntityDataSerializer.forValueType(VillagerEntitySimulated.DebugData.STREAM_CODEC));
}
