package com.maestreaux.dynasties.init;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.core.MarketAgent;
import net.minecraft.core.Rotations;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class ModEntityDataSerializers {
    public static DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, DynastiesMod.MODID);

    public static final RegistryObject<EntityDataSerializer<List<MarketAgent.TradeOffer>>> TRADE_OFFERS = ENTITY_DATA_SERIALIZERS.register("trade_offers", () -> new EntityDataSerializer.ForValueType<>() {
        public void write(FriendlyByteBuf buffer, List<MarketAgent.TradeOffer> offers) {
            buffer.writeInt(offers.size());

            for(var offer: offers) {
                buffer.writeItem(offer.getItemOffered().getDefaultInstance());
                buffer.writeInt(offer.getQuantityOffered());
                buffer.writeInt(offer.getQuantitySold());
                buffer.writeInt(offer.getPrice());
            }
        }

        public List<MarketAgent.TradeOffer> read(FriendlyByteBuf buffer) {
            List<MarketAgent.TradeOffer> offers = new ArrayList<>();
            int length = buffer.readInt();

            for (int i = 0; i < length; i++) {
                var newOffer = new MarketAgent.TradeOffer(buffer.readItem().getItem(), buffer.readInt(), buffer.readInt(), buffer.readInt());
                offers.add(newOffer);
            }

            return offers;
        }
    });
}
