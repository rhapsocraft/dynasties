package com.maestreaux.dynasties.world.items.debug;

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
                    if (!this.currentPlotStartPos.equals(newPos)) {
                        var posOffset = newPos.offset(0, newPos.getY() - this.currentPlotStartPos.getY(), 0);
                        parentZone.addPlot(this.currentPlotStartPos, posOffset, 2);

                        var addPlotPacket = new ZonePacket.CAddPlotPacket(parentZone.getUUID(), this.currentPlotStartPos, posOffset);
                        PacketHandler.sendToAll(addPlotPacket);

                        this.currentPlotStartPos = null;
                    }
                }

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }
}
