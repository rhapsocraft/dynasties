package com.maestreaux.dynasties.client.renderer.debug;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.core.simulation.SimulationState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DynastiesMod.MODID, value = Dist.CLIENT)
public class SimulatedEntityDebugRenderer {
    public static void renderSimulatedEntities(PoseStack matrixStack, MultiBufferSource.BufferSource bufferSource) {
        var entities = SimulationState.CLIENT_ENTITIES.values().stream().toList();
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();

        for (var entity: entities) {
            var entityPos = entity.getPos();

            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());

            matrixStack.pushPose();
            Vec3 cam = camera.getPosition();
            matrixStack.translate(-cam.x, -cam.y, -cam.z);

            ShapeRenderer.renderLineBox(matrixStack, vertexConsumer, entityPos.getX(), entityPos.getY(), entityPos.getZ(), entityPos.getX() + 1D, entityPos.getY() + 2D, entityPos.getZ() + 1D, 6.6f, 15.9f, 6.6f, 15.5f);

            matrixStack.popPose();
        }
    }
}
