package com.maestreaux.dynasties.mixin;

import com.maestreaux.dynasties.client.renderer.ZoneRenderer;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LevelRenderer.class)
public class RenderLevelMixin {
//    @Inject(at = @At("TAIL"), method = "renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V")
//    public void renderLevel(
//            GraphicsResourceAllocator graphicsResourceAllocator,
//            DeltaTracker deltaTracker,
//            boolean bl,
//            Camera camera,
//            GameRenderer gameRenderer,
//            Matrix4f matrix4f,
//            Matrix4f matrix4f2,
//            CallbackInfo ci) {
//        ZoneRenderer.renderZone(camera);
//    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;renderAfterTranslucents(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDD)V"),
            locals = LocalCapture.CAPTURE_FAILHARD, method = "lambda$addLateDebugPass$5(Lnet/minecraft/client/renderer/FogParameters;Lcom/mojang/blaze3d/resource/ResourceHandle;Lnet/minecraft/world/phys/Vec3;)V" )
    private void renderLevel(FogParameters fogParams, ResourceHandle resourceHandle, Vec3 vec3, CallbackInfo ci, PoseStack posestack, MultiBufferSource.BufferSource bufferSource) {
        ZoneRenderer.renderZone(posestack, bufferSource);
    }
}

