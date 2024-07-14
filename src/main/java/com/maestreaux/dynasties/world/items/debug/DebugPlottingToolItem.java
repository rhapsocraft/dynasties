package com.maestreaux.dynasties.world.items.debug;

import com.maestreaux.dynasties.core.utils.PlotUtils;
import com.maestreaux.dynasties.network.PacketHandler;
import com.maestreaux.dynasties.network.ZonePacket;
import com.maestreaux.dynasties.world.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.SimpleFoiledItem;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

public class DebugPlottingToolItem extends SimpleFoiledItem {
    private BlockPos currentPlotStartPos;

    public DebugPlottingToolItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
        if (!pContext.getLevel().isClientSide()) {
            var serverLevel = (ServerLevel) pContext.getLevel();
            var parentZone = Zone.getContainerZone(serverLevel, pContext.getClickedPos());

            if (parentZone != null) {
                if (pContext.getPlayer() != null && pContext.getPlayer().isShiftKeyDown()) {
                    parentZone.clearPlots();
                    return InteractionResult.SUCCESS;
                }

                if (this.currentPlotStartPos == null) {
                    this.currentPlotStartPos = pContext.getClickedPos();
                } else {
                    var newPos = pContext.getClickedPos();
                    var newPosZone =  Zone.getContainerZone(serverLevel, newPos);

                    if (newPosZone != null && !this.currentPlotStartPos.equals(newPos) && PlotUtils.isValidPlot(currentPlotStartPos, newPos, parentZone)) {
                        var endPosOffset = newPos.offset(-parentZone.getCenter().getX(), -this.currentPlotStartPos.getY() - 1, -parentZone.getCenter().getZ());
                        var startPosOffset = this.currentPlotStartPos.subtract(parentZone.getCenter());
                        var newPlot = parentZone.addPlot(startPosOffset, endPosOffset, 2);

                        PlotUtils.debugSetPartitions(newPlot);

                        var addPlotPacket = new ZonePacket.CAddPlotPacket(parentZone.getUUID(), startPosOffset, endPosOffset, newPlot.getPartitions());
                        PacketHandler.sendToAll(addPlotPacket);
                    }

                    this.currentPlotStartPos = null;
                }

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }
}
