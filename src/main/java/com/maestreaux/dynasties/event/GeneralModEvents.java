package com.maestreaux.dynasties.event;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.commands.ResetValuationsCommand;
import com.maestreaux.dynasties.core.simulation.SimulationState;
import com.maestreaux.dynasties.core.simulation.Simulator;
import com.maestreaux.dynasties.core.utils.PlotUtils;
import com.maestreaux.dynasties.init.ModBuildings;
import com.maestreaux.dynasties.init.ModItems;
import com.maestreaux.dynasties.network.PacketHandler;
import com.maestreaux.dynasties.network.message.CAddPlot;
import com.maestreaux.dynasties.network.message.CSimulatedEntitiesList;
import com.maestreaux.dynasties.network.message.CZonesList;
import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.Zone;
import com.maestreaux.dynasties.world.entities.DynastiesVillager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

@Mod.EventBusSubscriber(modid = DynastiesMod.MODID)
public class GeneralModEvents {
    private static void sendZonesListPacket(ServerLevel level, ServerPlayer player) {
        var packet = new CZonesList(Zone.getZones(level));
        PacketHandler.sendToPlayer(packet, player);
    }

    private static void sendSimulatedEntitiesPacket(ServerLevel level, ServerPlayer player) {
        var packet = new CSimulatedEntitiesList(SimulationState.getEntities(level));
        PacketHandler.sendToPlayer(packet, player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        var level = event.getEntity().level();

        if (!level.isClientSide()) {
            sendZonesListPacket((ServerLevel) level, (ServerPlayer) event.getEntity());
            sendSimulatedEntitiesPacket((ServerLevel) level, (ServerPlayer) event.getEntity());
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
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        new ResetValuationsCommand(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // Implement for only Overworld for now
            var level = event.getServer().getLevel(ServerLevel.OVERWORLD);

            var currentTick = event.getServer().getTickCount();
            if (currentTick % 10 == 0) {


                Simulator.doTick(level, currentTick);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        var house = ModBuildings.BUILDINGS;

        for (var houseEntry : house.getEntries()) {
            houseEntry.get().loadTemplate(event.getServer());
        }
    }

    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide()) {
            var serverLevel = (ServerLevel) event.getLevel();
            var hitPos = event.getHitVec().getBlockPos();

            if (event.getItemStack().is(ModItems.DEBUG_TOOL.get())) {
                if (!event.getEntity().isShiftKeyDown()) {
                    var level = event.getLevel();
                    var position = level.getBlockState(hitPos).isSuffocating(level, hitPos) ? hitPos.above() : hitPos;

                    var newZone = new Zone(serverLevel, position);

                    Zone.add(serverLevel, newZone);

                    for (int i = 0; i < 8; i++) {
                        var newVillager = new DynastiesVillager(serverLevel, newZone);
                        serverLevel.addFreshEntity(newVillager);
                        newVillager.moveTo(newZone.getCenter().above().getCenter());
                    }
                } else {
                    var newVillager = new DynastiesVillager(serverLevel);
                    serverLevel.addFreshEntity(newVillager);
                    newVillager.moveTo(event.getHitVec().getLocation());
                }
            } else if (event.getItemStack().is(ModItems.DEBUG_TOOL_PLOT_CONVERTER.get())) {
                var parentZone = Zone.getContainerZone(serverLevel, hitPos);
                var selectedPlot = parentZone.getPlots().stream().filter(plot -> PlotUtils.contains(plot.getAbsoluteStartPos(), plot.getAbsoluteEndPos(), hitPos)).findFirst().orElse(null);
                var player = event.getEntity();

                if (selectedPlot != null) {
                    if (player.isShiftKeyDown()) {
                        selectedPlot.enable();

                        selectedPlot.clearSlots();

                        if (selectedPlot.getType() == Plot.PlotType.RESIDENTIAL) {
                            selectedPlot.addSlot(Plot.Job.TRADER);
                            selectedPlot.addSlot(Plot.Job.WORKER);
                        } else if (selectedPlot.getType() == Plot.PlotType.RANCH) {
                            selectedPlot.addSlot(Plot.Job.TRADER);
                            selectedPlot.addSlot(Plot.Job.RANCHER);
                        } else if (selectedPlot.getType() == Plot.PlotType.HALL) {
                            selectedPlot.addSlot(Plot.Job.NOBLE);
                            selectedPlot.addSlot(Plot.Job.WORKER);
                            selectedPlot.addSlot(Plot.Job.TRADER);
                        } else if (selectedPlot.getType() == Plot.PlotType.MARKET) {
                            selectedPlot.addSlot(Plot.Job.TRADER);
                        }
                    } else {
                        var newType = switch (selectedPlot.getType()) {
                            case RESERVED -> Plot.PlotType.RESIDENTIAL;
                            case RESIDENTIAL -> Plot.PlotType.RANCH;
                            case RANCH -> Plot.PlotType.HALL;
                            case HALL -> Plot.PlotType.MARKET;
                            default -> Plot.PlotType.RESERVED;
                        };

                        selectedPlot.setType(newType);
                        selectedPlot.clearPartitions();
                        PlotUtils.debugSetPartitions(selectedPlot);

                        var addPlotPacket = new CAddPlot(parentZone, selectedPlot);
                        PacketHandler.sendToAll(addPlotPacket);
                    }

                    parentZone.save((ServerLevel) player.level());
                }
            } else if (event.getItemStack().is(Items.STICK)) {
                Zone.getZones(serverLevel);
            }
        }
    }
}

