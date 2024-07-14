package com.maestreaux.dynasties.client.renderer;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.Partition;
import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.Zone;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = DynastiesMod.MODID, value = Dist.CLIENT)
public class ZoneRenderer {
    public static void drawZoneSquare(PoseStack matrixStack, Camera camera, Vec3i surfacePos) {
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.debugQuads());

        Vec3 cam = camera.getPosition();

        matrixStack.pushPose();

        matrixStack.translate(-cam.x, -cam.y, -cam.z);

        Matrix4f mat = matrixStack.last().pose();

        vertexConsumer.vertex(mat, surfacePos.getX(), surfacePos.getY() + 1.001F, surfacePos.getZ()).color(0, 255, 255, 150).endVertex();
        vertexConsumer.vertex(mat, surfacePos.getX() + 1, surfacePos.getY() + 1.001F, surfacePos.getZ()).color(0, 255, 255, 150).endVertex();
        vertexConsumer.vertex(mat, surfacePos.getX() + 1, surfacePos.getY() + 1.001F, surfacePos.getZ() + 1).color(0, 255, 255, 150).endVertex();
        vertexConsumer.vertex(mat, surfacePos.getX(), surfacePos.getY() + 1.001F, surfacePos.getZ() + 1).color(0, 255, 255, 150).endVertex();

        matrixStack.popPose();

        buffer.endBatch(RenderType.debugQuads());
    }

    private static List<Vec3i> getZoneSurfaces(AABB boundingBox, ClientLevel level) {
        // TODO: Cache with timed expiry
        return BlockPos.betweenClosedStream(boundingBox).filter(pos -> !level.getBlockState(pos.above()).isSuffocating(level, pos) && level.getBlockState(pos).isSuffocating(level, pos)).map((pos) -> new Vec3i(pos.getX(), pos.getY(), pos.getZ())).toList();
    }

    public static void drawZoneHighlight(PoseStack matrixStack, Camera camEntity, ClientLevel level, Zone zone) {
        var surfaces = getZoneSurfaces(zone.getBoundingBox().deflate(0.5), level);

        for (Vec3i surface: surfaces) {
            drawZoneSquare(matrixStack, camEntity, surface);
        }
    }

    public static void drawZoneBox(PoseStack matrixStack, Camera camera, Zone zone) {
        // RenderSystem.depthMask(false); // disable showing lines through blocks
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lines());

        matrixStack.pushPose();
        Vec3 cam = camera.getPosition();
        matrixStack.translate(-cam.x, -cam.y, -cam.z); // because we start at 0,0,0 relative to camera
        LevelRenderer.renderLineBox(matrixStack, vertexConsumer, zone.getBoundingBox(), 15.9f, 15.9f, 15.9f, 15.5f);
        LevelRenderer.renderLineBox(matrixStack, vertexConsumer, zone.getBoundingBox().inflate(8F), 15, 0F, 0F, 15.5f);
        matrixStack.popPose();

        buffer.endBatch(RenderType.lines());
    }

    private static int mostSouthernEast(Vec3i pos1, Vec3i pos2) {
        if (pos1.getX() + pos1.getZ() > pos2.getX() + pos2.getZ()) {
            return -1;
        } else {
            return 1;
        }
    }

    public static void drawPartitions(PoseStack matrixStack, Camera camera, Plot plot) {
        for (var partition: plot.getPartitions()) {
            var partitionStartPos = partition.getOrigin().offset(plot.getAbsoluteStartPos());
            var partitionEndPos = partitionStartPos.offset(partition.getWidth(), 0, partition.getLength());

            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.debugQuads());

            matrixStack.pushPose();
            Vec3 cam = camera.getPosition();
            matrixStack.translate(-cam.x, -cam.y, -cam.z);
            Matrix4f mat = matrixStack.last().pose();

            vertexConsumer.vertex(mat, (float) partitionStartPos.getX(), (float) partitionStartPos.getY() + 1.006F, (float) partitionStartPos.getZ()).color(100, 220, 100, 150).endVertex();
            vertexConsumer.vertex(mat, (float) partitionEndPos.getX(), (float) partitionStartPos.getY() + 1.006F, (float) partitionStartPos.getZ()).color(100, 220, 100, 150).endVertex();
            vertexConsumer.vertex(mat, (float) partitionEndPos.getX(), (float) partitionStartPos.getY() + 1.006F, (float) partitionEndPos.getZ()).color(100, 220, 100, 150).endVertex();
            vertexConsumer.vertex(mat, (float) partitionStartPos.getX(), (float) partitionStartPos.getY() + 1.006F, (float) partitionEndPos.getZ()).color(100, 220, 100, 150).endVertex();

            matrixStack.popPose();

            buffer.endBatch(RenderType.debugQuads());
        }

    }

    public static void drawZonePlots(PoseStack matrixStack, Camera camera, Zone zone) {
        var plots = zone.getPlots();

        for (var plot: plots) {
            var plotStartPos = plot.getStartPos().offset(zone.getCenter());
            var plotEndPos = plot.getEndPos().offset(zone.getCenter()).offset(1, 0, 1);
            //var corner1 = new BlockPos(plotStartPos.getX(), plotStartPos.getY(), plotEndPos.getZ());
            //var corner2 = new BlockPos(plotEndPos.getX(), plotStartPos.getY(), plotStartPos.getZ());

            //List<BlockPos> list = ObjectArrayList.of(plotStartPos, plotEndPos, corner1, corner2);
            //.sort(ZoneRenderer::mostSouthernEast);

            //var closestPos = ((BlockPos) list.toArray()[0]).offset(1, 0, 1);
            //var furthestPos = ((BlockPos) list.toArray()[list.size() - 1]);

            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lines());

            matrixStack.pushPose();
            Vec3 cam = camera.getPosition();
            matrixStack.translate(-cam.x, -cam.y, -cam.z);

            LevelRenderer.renderLineBox(matrixStack, vertexConsumer, plotStartPos.getX(), plotStartPos.getY() + 1.005D, plotStartPos.getZ(), plotEndPos.getX(), plotStartPos.getY() + 1.005D, plotEndPos.getZ(), 6.6f, 15.9f, 6.6f, 15.5f);

            matrixStack.popPose();
            buffer.endBatch(RenderType.lines());

            drawPartitions( matrixStack, camera, plot);
        }


    }

    @SubscribeEvent
    public static void onRenderWorldEvent(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            var level = Minecraft.getInstance().level;

            // TODO: Zones by Level
            for(var zone: Zone.getZones()) {
                if (zone != null) {
                    drawZoneBox(event.getPoseStack(), event.getCamera(), zone);
                    drawZoneHighlight(event.getPoseStack(), event.getCamera(), level, zone);
                    drawZonePlots(event.getPoseStack(), event.getCamera(), zone);
                }
            }
        }
    }
}
