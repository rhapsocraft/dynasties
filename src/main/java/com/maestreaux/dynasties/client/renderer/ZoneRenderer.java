package com.maestreaux.dynasties.client.renderer;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.core.utils.PlotUtils;
import com.maestreaux.dynasties.init.ModItems;
import com.maestreaux.dynasties.world.Partition;
import com.maestreaux.dynasties.world.Plot;
import com.maestreaux.dynasties.world.Zone;
import com.maestreaux.dynasties.world.items.debug.DebugPlottingToolItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = DynastiesMod.MODID, value = Dist.CLIENT)
public class ZoneRenderer {
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
    public static void drawZoneBox(PoseStack poseStack, Camera camera, MultiBufferSource.BufferSource bufferSource, Zone zone) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());

        poseStack.pushPose();
        Vec3 cam = camera.getPosition();
        poseStack.translate(-cam.x, -cam.y, -cam.z);
        ShapeRenderer.renderLineBox(poseStack, vertexConsumer,  zone.getBoundingBox(), 6.6f, 15.9f, 6.6f, 15.5f);
        ShapeRenderer.renderLineBox(poseStack, vertexConsumer,  zone.getBoundingBox().inflate(16D), 1f, 15.5f, 15.5f, 15.5f);
        poseStack.popPose();
        bufferSource.endBatch(RenderType.lines());

        if (!poseStack.clear()) {
            throw new IllegalStateException("Pose stack not empty");
        }
    }

    public static void drawPartitions(PoseStack matrixStack, Camera camera, MultiBufferSource.BufferSource bufferSource, Plot plot) {
        for (var partition: plot.getPartitions()) {
            var partitionStartPos = partition.getOrigin().offset(plot.getAbsoluteStartPos());
            var partitionEndPos = partitionStartPos.offset(partition.getWidth(), 0, partition.getLength());

            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());

            matrixStack.pushPose();
            Vec3 cam = camera.getPosition();
            matrixStack.translate(-cam.x, -cam.y, -cam.z);
            //Matrix4f mat = matrixStack.last().pose();

            int pRed = partition.getPartitionType() == Partition.PartitionType.HOME ? 220 : 100;
            int pGreen = partition.getPartitionType() == Partition.PartitionType.HOME ? 100 : 220;

            ShapeRenderer.renderLineBox(matrixStack, vertexConsumer, partitionStartPos.getX(), partitionStartPos.getY() + 1.005D, partitionStartPos.getZ(), partitionEndPos.getX(), partitionEndPos.getY() + 1.005D, partitionEndPos.getZ(), 15.9f, 6.6f, 6.6f, 8.5f);
//            vertexConsumer.vertex(mat, (float) partitionStartPos.getX(), (float) partitionStartPos.getY() + 1.006F, (float) partitionStartPos.getZ()).color(pRed, pGreen, 100, 100).endVertex();
//            vertexConsumer.vertex(mat, (float) partitionEndPos.getX(), (float) partitionStartPos.getY() + 1.006F, (float) partitionStartPos.getZ()).color(pRed, pGreen, 100, 100).endVertex();
//            vertexConsumer.vertex(mat, (float) partitionEndPos.getX(), (float) partitionStartPos.getY() + 1.006F, (float) partitionEndPos.getZ()).color(pRed, pGreen, 100, 100).endVertex();
//            vertexConsumer.vertex(mat, (float) partitionStartPos.getX(), (float) partitionStartPos.getY() + 1.006F, (float) partitionEndPos.getZ()).color(pRed, pGreen, 100, 100).endVertex();

            matrixStack.popPose();
            bufferSource.endBatch(RenderType.lines());

            bufferSource.endBatch(RenderType.debugQuads());
        }
    }

    public static void drawPrePlot(PoseStack poseStack, Camera camera, MultiBufferSource.BufferSource bufferSource, Zone zone) {
        var lastSelectedPos = DebugPlottingToolItem.LAST_SELECTED_BLOCK_POS;
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player != null && player.getItemHeldByArm(player.getMainArm()).getItem() == ModItems.DEBUG_TOOL_PLOT.get()) {
            var blockHitResult = player.pick(5D, 0F, false);

            if (lastSelectedPos != null && blockHitResult.getType() == HitResult.Type.BLOCK) {
                poseStack.pushPose();
                Vec3 cam = camera.getPosition();
                poseStack.translate(-cam.x, -cam.y, -cam.z);

                BlockPos blockPosLookingAt = ((BlockHitResult) blockHitResult).getBlockPos();
                var pRed = 1f;
                var pGreen = 15.5f;
                var pBlue = 15.5f;

                if (PlotUtils.isValidPlot(lastSelectedPos, blockPosLookingAt, zone)) {
                    pRed = 15.5f;
                    pGreen = 1f;
                    pBlue = 15.5f;
                }

                var startPos = new Vector3f(Math.max(blockPosLookingAt.getX() + 1.0F, lastSelectedPos.getX() + 1.0F), lastSelectedPos.getY(), Math.max(blockPosLookingAt.getZ() + 1.0F, lastSelectedPos.getZ() + 1.0F));
                var endPos = new Vector3f(Math.min(blockPosLookingAt.getX(), lastSelectedPos.getX()), lastSelectedPos.getY(), Math.min(blockPosLookingAt.getZ(), lastSelectedPos.getZ()));

                VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.debugQuads());
                ShapeRenderer.renderFace(poseStack, vertexConsumer, Direction.UP, startPos.x,  startPos.y + 1.1F, startPos.z, endPos.x, startPos.y + 1.1F, endPos.z, pRed, pGreen, pBlue, 15.5f);

                VertexConsumer linesVertexConsumer = bufferSource.getBuffer(RenderType.lines());
                ShapeRenderer.renderLineBox(poseStack, linesVertexConsumer, startPos.x,  startPos.y + 1.1F, startPos.z, endPos.x, startPos.y + 1.1F, endPos.z, pRed, pGreen, pBlue, 15.9f);

                poseStack.popPose();
                bufferSource.endBatch(RenderType.debugQuads());
            }
        }
    }

    public static void drawZonePlots(PoseStack matrixStack, Camera camera, MultiBufferSource.BufferSource bufferSource, Zone zone) {
        var plots = zone.getPlots();
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        for (var plot : plots) {
            var plotStartPos = plot.getStartPos().offset(zone.getCenter());
            var plotEndPos = plot.getEndPos().offset(zone.getCenter()).offset(1, 0, 1);

            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());

            matrixStack.pushPose();
            Vec3 cam = camera.getPosition();
            matrixStack.translate(-cam.x, -cam.y, -cam.z);

            ShapeRenderer.renderLineBox(matrixStack, vertexConsumer, plotStartPos.getX(), plotStartPos.getY() + 1.005D, plotStartPos.getZ(), plotEndPos.getX(), plotStartPos.getY() + 1.005D, plotEndPos.getZ(), 6.6f, 15.9f, 6.6f, 15.5f);

            matrixStack.popPose();
            bufferSource.endBatch(RenderType.lines());

            drawPartitions(matrixStack, camera, bufferSource, plot);
        }
    }

    public static void drawZonePlotNames(PoseStack matrixStack, Camera camera, MultiBufferSource.BufferSource bufferSource, Zone zone) {
        var plots = zone.getPlots();
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        for (var plot : plots) {
            var plotStartPos = plot.getStartPos().offset(zone.getCenter());
            var plotEndPos = plot.getEndPos().offset(zone.getCenter()).offset(1, 0, 1);

            matrixStack.pushPose();
            Vec3 cam = camera.getPosition();
            matrixStack.translate((double) ((plot.getAbsoluteStartPos().getX() + plot.getAbsoluteEndPos().getX()) / 2) + 1D, plot.getAbsoluteStartPos().getY() + (double)7F,  (double) ((plot.getAbsoluteStartPos().getZ() + plot.getAbsoluteEndPos().getZ()) / 2) + 1D);
            matrixStack.translate(-cam.x, -cam.y, -cam.z);
            matrixStack.mulPose(camera.rotation());
            matrixStack.scale(0.025F, -0.025F, 0.025F);

            Matrix4f matrix4f = matrixStack.last().pose();

            int j = (int)(minecraft.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
            font.drawInBatch(plot.getTypeName(), (float)(-font.width(plot.getTypeName())) / 2.0F, 0.0F, -1,false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, j, LightTexture.lightCoordsWithEmission(15728640, 2));

            matrixStack.popPose();
        }
    }

    public static void drawVillagerInfo(PoseStack matrixStack, Camera camera,  MultiBufferSource.BufferSource bufferSource) {
        Minecraft minecraft = Minecraft.getInstance();
        var player = minecraft.player;

    }

    public static void renderZone(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();

        for (var zone : Zone.getZones()) {
            drawZoneBox(poseStack, camera, bufferSource, zone);
            drawZonePlots(poseStack, camera, bufferSource, zone);
            drawZonePlotNames(poseStack, camera, bufferSource, zone);
            drawPrePlot(poseStack, camera, bufferSource, zone);
        }
    }
}
