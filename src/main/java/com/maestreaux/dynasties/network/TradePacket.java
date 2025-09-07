package com.maestreaux.dynasties.network;

import com.maestreaux.dynasties.core.MarketAgent;
import com.maestreaux.dynasties.world.entities.base.AbstractDynastyVillager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class TradePacket {
    public static class SBuyFromTraderPacket {
        private final UUID traderUUID;
        private final ItemStack itemToBuy;
        private final int amountToBuy;

        public SBuyFromTraderPacket(UUID traderUUID, ItemStack itemToBuy, int amountToBuy) {
            this.traderUUID = traderUUID;
            this.itemToBuy = itemToBuy;
            this.amountToBuy = amountToBuy;
        }

        public SBuyFromTraderPacket(FriendlyByteBuf buffer) {
            this.traderUUID = buffer.readUUID();
            this.itemToBuy = buffer.readItem();
            this.amountToBuy = buffer.readInt();
        }

        public void encode(FriendlyByteBuf buffer) {
            buffer.writeUUID(this.traderUUID);
            buffer.writeItem(this.itemToBuy);
            buffer.writeInt(this.amountToBuy);
        }

        public void handle(Supplier<NetworkEvent.Context> context) {
            var ctx = context.get();

            ctx.enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handlePacket(context));
            });

            ctx.setPacketHandled(true);
        }

        public void handlePacket(Supplier<NetworkEvent.Context> context) {
            var player = context.get().getSender();
            var level = (ServerLevel) player.level();

            var trader = (AbstractDynastyVillager) level.getEntity(traderUUID);
            if (trader != null) {
                var marketAgent = trader.asMarketAgent();
                var offer = marketAgent.getActiveOffers().get(this.itemToBuy.getItem());

                offer.sell(this.amountToBuy);
            }
        }
    }
}
