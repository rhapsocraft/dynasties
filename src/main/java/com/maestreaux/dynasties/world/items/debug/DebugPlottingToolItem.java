package com.maestreaux.dynasties.world.items.debug;

import com.maestreaux.dynasties.core.utils.PlotUtils;
import com.maestreaux.dynasties.network.PacketHandler;
import com.maestreaux.dynasties.network.message.CAddPlot;
import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

public class DebugPlottingToolItem extends Item {
    private BlockPos currentPlotStartPos;
    public static BlockPos LAST_SELECTED_BLOCK_POS = null;

    public DebugPlottingToolItem(Item.Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
        if (!pContext.getLevel().isClientSide()) {
            var serverLevel = (ServerLevel) pContext.getLevel();
            var parentZone = Zone.getContainerZone(serverLevel, pContext.getClickedPos());
            var player = pContext.getPlayer();

            if (parentZone != null) {
                if (this.currentPlotStartPos == null) {
                    this.currentPlotStartPos = pContext.getClickedPos();
//                    Plot overlappingPlot = parentZone.getPlots().stream().filter((plot) -> PlotUtils.contains(plot.getAbsoluteStartPos(), plot.getAbsoluteEndPos(), clickedPos)).findFirst().orElse(null);
//
//                    if (overlappingPlot != null) {
//                        var newType = switch (overlappingPlot.getType()) {
//                            case RESERVED -> Plot.PlotType.RESIDENTIAL;
//                            case RESIDENTIAL -> Plot.PlotType.SQUARE;
//                            default -> Plot.PlotType.RESERVED;
//                        };
//
//                        if (player != null && player.isShiftKeyDown()) {
//                            newType = Plot.PlotType.SQUARE;
//                        }
//
//                        overlappingPlot.setType(newType);
//                        overlappingPlot.clearPartitions();
//
//                        if (overlappingPlot.getType() == Plot.PlotType.RESIDENTIAL) {
//                            overlappingPlot.addSlot(Plot.SlotJob.TRADER);
//                            overlappingPlot.addSlot(Plot.SlotJob.WORKER);
//                        } else if (overlappingPlot.getType() == Plot.PlotType.SQUARE) {
//                            overlappingPlot.addSlot(Plot.SlotJob.WORKER);
//                            overlappingPlot.addSlot(Plot.SlotJob.WORKER);
//                            overlappingPlot.addSlot(Plot.SlotJob.NOBLE);
//                        }
//
//                        PlotUtils.debugSetPartitions(overlappingPlot);
//
//                        var zonesList = new CZonesList(Zone.getZones());
//                        PacketHandler.sendToAll(zonesList);
//                    }
                } else {
                    var newPos = pContext.getClickedPos();
                    var newPosZone = Zone.getContainerZone(serverLevel, newPos);

                    if (newPosZone != null && !this.currentPlotStartPos.equals(newPos) && PlotUtils.isValidPlot(currentPlotStartPos, newPos, parentZone)) {
                        var endPosOffset = newPos.offset(-parentZone.getCenter().getX(), -this.currentPlotStartPos.getY() - 1, -parentZone.getCenter().getZ());
                        var startPosOffset = this.currentPlotStartPos.subtract(parentZone.getCenter());
                        var newPlot = parentZone.addPlot(startPosOffset, endPosOffset, Plot.PlotType.RESERVED);

                        var addPlotPacket = new CAddPlot(parentZone, newPlot);
                        PacketHandler.sendToAll(addPlotPacket);
                    }

                    this.currentPlotStartPos = null;
                }

                return InteractionResult.SUCCESS_SERVER;
            }
        } else {
            LAST_SELECTED_BLOCK_POS = LAST_SELECTED_BLOCK_POS == null ? pContext.getClickedPos() : null;

            var player = pContext.getPlayer();

            if (player != null && player.isShiftKeyDown()) {
                LAST_SELECTED_BLOCK_POS = null;
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
