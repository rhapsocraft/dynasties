package com.maestreaux.dynasties.event;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.init.ModBuildings;
import com.maestreaux.dynasties.init.ModItems;
import com.maestreaux.dynasties.network.PacketHandler;
import com.maestreaux.dynasties.network.message.CZonesList;
import com.maestreaux.dynasties.world.Zone;
import com.maestreaux.dynasties.world.entities.DynastiesVillager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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

    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide()) {
            var serverLevel = (ServerLevel) event.getLevel();

            if (event.getItemStack().is(ModItems.DEBUG_TOOL.get())) {
                var hitPos = event.getHitVec().getBlockPos();
                var level = event.getLevel();
                var position = level.getBlockState(hitPos).isSuffocating(level, hitPos) ?  hitPos.above() : hitPos;

                var newZone = new Zone(serverLevel, position);

                Zone.add(serverLevel, newZone);

                for(int i = 0; i < 6; i++) {
                    var newVillager = new DynastiesVillager(serverLevel, newZone);
                    serverLevel.addFreshEntity(newVillager);
                    newVillager.moveTo(newZone.getCenter().above().getCenter());
                }

            } else if (event.getItemStack().is(Items.STICK)) {
                Zone.getZones(serverLevel);
            }
        }
    }
}
