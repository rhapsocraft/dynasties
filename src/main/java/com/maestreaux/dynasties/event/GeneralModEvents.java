package com.maestreaux.dynasties.event;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.network.PacketHandler;
import com.maestreaux.dynasties.network.ZonePacket;
import com.maestreaux.dynasties.world.Zone;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DynastiesMod.MODID)
public class GeneralModEvents {
    private static void sendZonesListPacket(ServerLevel level, ServerPlayer player) {
        ZonePacket.CZonesPacket packet = new ZonePacket.CZonesPacket(Zone.getZones(level));
        PacketHandler.sendToPlayer(packet, player);
    }
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        var level = event.getEntity().level();

        if (!level.isClientSide()) {
            sendZonesListPacket((ServerLevel) level, (ServerPlayer) event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimensions(PlayerEvent.PlayerChangedDimensionEvent event) {
        var level = event.getEntity().level();

        if (!level.isClientSide()) {
            sendZonesListPacket((ServerLevel) level, (ServerPlayer) event.getEntity());
        }
    }
}
