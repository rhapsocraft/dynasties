package com.maestreaux.dynasties.network.message;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record SBuyFromTrader(UUID traderUUID, ItemStack itemToBuy, int amountToBuy ) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, "buy_from_trader_message");
    public static final Type<CAddZone> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SBuyFromTrader> STREAM_CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, SBuyFromTrader::traderUUID, ItemStack.STREAM_CODEC, SBuyFromTrader::itemToBuy, ByteBufCodecs.INT, SBuyFromTrader::amountToBuy, SBuyFromTrader::new);

    public static void handle(SBuyFromTrader message, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                var player = context.getSender();

                if (player != null) {
                    var level = (ServerLevel) player.level();

                    var trader = (AbstractDynastyVillager) level.getEntity(message.traderUUID);
                    if (trader != null) {
                        var marketAgent = trader.getSimEntity().asMarketAgent();
                        var offer = marketAgent.getActiveOffers().get(message.itemToBuy.getItem());

                        var boughtItems = offer.sell(message.amountToBuy);

                        if (boughtItems != null) {
                            player.getInventory().add(boughtItems);
                        }
                    }
                }
            });
        });

        context.setPacketHandled(true);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
