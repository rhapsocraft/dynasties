package com.maestreaux.dynasties.client.renderer.layer;

import com.maestreaux.dynasties.client.renderer.entity.state.DynastiesVillagerRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;

public class VillagerItemInHandLayer<S extends DynastiesVillagerRenderState, M extends EntityModel<S> & ArmedModel> extends RenderLayer<S, M> {
    public VillagerItemInHandLayer(RenderLayerParent<S, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S renderState, float v, float v1) {
        this.renderArmWithItem(renderState, renderState.rightHandItem, HumanoidArm.RIGHT, poseStack, multiBufferSource, i);
        this.renderArmWithItem(renderState, renderState.leftHandItem, HumanoidArm.LEFT, poseStack, multiBufferSource, i);
    }

    protected void renderArmWithItem(S renderState, ItemStackRenderState itemStackRenderState, HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource, int p_117191_) {
        if (!itemStackRenderState.isEmpty()) {
            poseStack.pushPose();
            this.getParentModel().translateToHand(humanoidArm, poseStack);
            if (!renderState.isHoldingBlock) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

                poseStack.translate(0F, 0.25F, -0.35F);
            } else {
                poseStack.mulPose(Axis.YP.rotationDegrees(205.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(0.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-50.0F));

                poseStack.translate(-0.7F, -0.2F, 0.15F);
                poseStack.scale(1.3F, 1.3F, 1.3F);
            }
            itemStackRenderState.render(poseStack, multiBufferSource, p_117191_, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }
}
