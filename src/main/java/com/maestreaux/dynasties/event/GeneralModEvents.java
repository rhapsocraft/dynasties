package com.maestreaux.dynasties.event;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.init.ModBuildings;
import com.maestreaux.dynasties.network.PacketHandler;
import com.maestreaux.dynasties.network.message.CZonesList;
import com.maestreaux.dynasties.world.Zone;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DynastiesMod.MODID)
public class GeneralModEvents {
    private static void sendZonesListPacket(ServerLevel level, ServerPlayer player) {
        var packet = new CZonesList(Zone.getZones(level));
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

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        var house = ModBuildings.BUILDINGS;

        for ( var houseEntry : house.getEntries()) {
            houseEntry.get().loadTemplate(event.getServer());
        }
    }
}
