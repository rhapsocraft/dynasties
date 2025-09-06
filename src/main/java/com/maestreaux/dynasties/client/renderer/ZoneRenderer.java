package com.maestreaux.dynasties.client.renderer;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.world.Partition;
import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.Zone;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.List;

@Mod.EventBusSubscriber(modid = DynastiesMod.MODID, value = Dist.CLIENT)


public class ZoneRenderer {
    private static final CrossFrameResourcePool resourcePool = new CrossFrameResourcePool(3);
//    public static void drawZoneSquare(PoseStack matrixStack, Camera camera, Vec3i surfacePos) {
//        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
//        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.debugQuads());
//
//        Vec3 cam = camera.getPosition();
//
//        matrixStack.pushPose();
//
//        matrixStack.translate(-cam.x, -cam.y, -cam.z);
//
//        Matrix4f mat = matrixStack.last().pose();
//
//        vertexConsumer.vertex(mat, surfacePos.getX(), surfacePos.getY() + 1.001F, surfacePos.getZ()).color(0, 255, 255, 80).endVertex();
//        vertexConsumer.vertex(mat, surfacePos.getX() + 1, surfacePos.getY() + 1.001F, surfacePos.getZ()).color(0, 255, 255, 80).endVertex();
//        vertexConsumer.vertex(mat, surfacePos.getX() + 1, surfacePos.getY() + 1.001F, surfacePos.getZ() + 1).color(0, 255, 255, 80).endVertex();
//        vertexConsumer.vertex(mat, surfacePos.getX(), surfacePos.getY() + 1.001F, surfacePos.getZ() + 1).color(0, 255, 255, 80).endVertex();
//
//        matrixStack.popPose();
//
//        buffer.endBatch(RenderType.debugQuads());
//    }
//
//    private static List<Vec3i> getZoneSurfaces(AABB boundingBox, ClientLevel level) {
//        // TODO: Cache with timed expiry
//        return BlockPos.betweenClosedStream(boundingBox).filter(pos -> !level.getBlockState(pos.above()).isSuffocating(level, pos) && level.getBlockState(pos).isSuffocating(level, pos)).map((pos) -> new Vec3i(pos.getX(), pos.getY(), pos.getZ())).toList();
//    }
//
//    public static void drawZoneHighlight(PoseStack matrixStack, Camera camEntity, ClientLevel level, Zone zone) {
//        var surfaces = getZoneSurfaces(zone.getBoundingBox().deflate(0.5), level);
//
//        for (Vec3i surface: surfaces) {
//            drawZoneSquare(matrixStack, camEntity, surface);
//        }
//    }
//
//    public static void drawZoneBox(PoseStack matrixStack, Camera camera, Zone zone) {
//        // RenderSystem.depthMask(false); // disable showing lines through blocks
//        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
//        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lines());
//
//        matrixStack.pushPose();
//        Vec3 cam = camera.getPosition();
//        matrixStack.translate(-cam.x, -cam.y, -cam.z); // because we start at 0,0,0 relative to camera
//        LevelRenderer.renderLineBox(matrixStack, vertexConsumer, zone.getBoundingBox(), 15.9f, 15.9f, 15.9f, 15.5f);
//        LevelRenderer.renderLineBox(matrixStack, vertexConsumer, zone.getBoundingBox().inflate(8F), 15, 0F, 0F, 15.5f);
//        matrixStack.popPose();
//
//        buffer.endBatch(RenderType.lines());
//    }
//
//    public static void drawPartitions(PoseStack matrixStack, Camera camera, Plot plot) {
//        for (var partition: plot.getPartitions()) {
//            var partitionStartPos = partition.getOrigin().offset(plot.getAbsoluteStartPos());
//            var partitionEndPos = partitionStartPos.offset(partition.getWidth(), 0, partition.getLength());
//
//            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
//            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lines());
//
//            matrixStack.pushPose();
//            Vec3 cam = camera.getPosition();
//            matrixStack.translate(-cam.x, -cam.y, -cam.z);
//            //Matrix4f mat = matrixStack.last().pose();
//
//            int pRed = partition.getPartitionType() == Partition.PartitionType.HOME ? 220 : 100;
//            int pGreen = partition.getPartitionType() == Partition.PartitionType.HOME ? 100 : 220;
//
//            LevelRenderer.renderLineBox(matrixStack, vertexConsumer, partitionStartPos.getX(), partitionStartPos.getY() + 1.005D, partitionStartPos.getZ(), partitionEndPos.getX(), partitionEndPos.getY() + 1.005D, partitionEndPos.getZ(), 15.9f, 6.6f, 6.6f, 8.5f);
////            vertexConsumer.vertex(mat, (float) partitionStartPos.getX(), (float) partitionStartPos.getY() + 1.006F, (float) partitionStartPos.getZ()).color(pRed, pGreen, 100, 100).endVertex();
////            vertexConsumer.vertex(mat, (float) partitionEndPos.getX(), (float) partitionStartPos.getY() + 1.006F, (float) partitionStartPos.getZ()).color(pRed, pGreen, 100, 100).endVertex();
////            vertexConsumer.vertex(mat, (float) partitionEndPos.getX(), (float) partitionStartPos.getY() + 1.006F, (float) partitionEndPos.getZ()).color(pRed, pGreen, 100, 100).endVertex();
////            vertexConsumer.vertex(mat, (float) partitionStartPos.getX(), (float) partitionStartPos.getY() + 1.006F, (float) partitionEndPos.getZ()).color(pRed, pGreen, 100, 100).endVertex();
//
//            matrixStack.popPose();
//            buffer.endBatch(RenderType.lines());
//
////            buffer.endBatch(RenderType.debugQuads());
//        }
//
//    }
//
//    public static void drawZonePlots(PoseStack matrixStack, Camera camera, Zone zone) {
//        var plots = zone.getPlots();
//
//        for (var plot: plots) {
//            var plotStartPos = plot.getStartPos().offset(zone.getCenter());
//            var plotEndPos = plot.getEndPos().offset(zone.getCenter()).offset(1, 0, 1);
//            //var corner1 = new BlockPos(plotStartPos.getX(), plotStartPos.getY(), plotEndPos.getZ());
//            //var corner2 = new BlockPos(plotEndPos.getX(), plotStartPos.getY(), plotStartPos.getZ());
//
//            //List<BlockPos> list = ObjectArrayList.of(plotStartPos, plotEndPos, corner1, corner2);
//            //.sort(ZoneRenderer::mostSouthernEast);
//
//            //var closestPos = ((BlockPos) list.toArray()[0]).offset(1, 0, 1);
//            //var furthestPos = ((BlockPos) list.toArray()[list.size() - 1]);
//
//            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
//            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lines());
//
//            matrixStack.pushPose();
//            Vec3 cam = camera.getPosition();
//            matrixStack.translate(-cam.x, -cam.y, -cam.z);
//
//            LevelRenderer.renderLineBox(matrixStack, vertexConsumer, plotStartPos.getX(), plotStartPos.getY() + 1.005D, plotStartPos.getZ(), plotEndPos.getX(), plotStartPos.getY() + 1.005D, plotEndPos.getZ(), 6.6f, 15.9f, 6.6f, 15.5f);
//
//            matrixStack.popPose();
//            buffer.endBatch(RenderType.lines());
//
//            drawPartitions( matrixStack, camera, plot);
//        }
//
//
//    }

    @SubscribeEvent
    public static void onRenderWorldEvent(TickEvent.RenderTickEvent.Post event) {
        FrameGraphBuilder fgBuilder = new FrameGraphBuilder();
        Minecraft minecraft = Minecraft.getInstance();

        var target = fgBuilder.importExternal("main", minecraft.getMainRenderTarget());
        var pass = fgBuilder.addPass("custom_debug");

        pass.readsAndWrites(target);

        pass.executes(() -> {
            target.get().bindWrite(false);
            MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
            Camera camera = minecraft.gameRenderer.getMainCamera();

            for(var zone: Zone.getZones()) {
                PoseStack poseStack = new PoseStack();
                poseStack.pushPose();
                Vec3 cam = camera.getPosition();
                poseStack.translate(-cam.x, -cam.y, -cam.z);
                //poseStack.translate(zone.getCenter().getX(), zone.getCenter().getY(),  zone.getCenter().getZ());
                VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());
                ShapeRenderer.renderLineBox(poseStack, vertexConsumer, zone.getBoundingBox(), 6.6f, 15.9f, 6.6f, 15.5f);
                poseStack.popPose();
                bufferSource.endBatch(RenderType.lines());

                if (!poseStack.clear()) {
                    throw new IllegalStateException("Pose stack not empty");
                }
            }
        });

        fgBuilder.execute(resourcePool);


//        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
//            var level = Minecraft.getInstance().level;
//
//            // TODO: Zones by Level
//            for(var zone: Zone.getZones()) {
//                if (zone != null) {
//                    drawZoneBox(event.getPoseStack(), event.getCamera(), zone);
//                    // drawZoneHighlight(event.getPoseStack(), event.getCamera(), level, zone);
//                    drawZonePlots(event.getPoseStack(), event.getCamera(), zone);
//                }
//            }
//        }
    }
}
