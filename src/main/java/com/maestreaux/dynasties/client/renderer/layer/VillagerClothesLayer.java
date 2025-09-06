package com.maestreaux.dynasties.client.renderer.layer;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.client.renderer.entity.state.DynastiesVillagerRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class VillagerClothesLayer<S extends DynastiesVillagerRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
    // TODO: TEMPORARY NOBILITY FLAG
    private static final ResourceLocation NOBILITY_CLOTHES = ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, "textures/entity/clothes/nobility_1.png");

    public VillagerClothesLayer(RenderLayerParent<S, M> p_117346_) {
        super(p_117346_);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S renderState, float v, float v1) {
        if (!renderState.isInvisible) {
            if (renderState.isNobility) {
                M model = this.getParentModel();
                renderColoredCutoutModel(model, NOBILITY_CLOTHES, poseStack, multiBufferSource, i, renderState, -1);
            }
        }
    }
}
