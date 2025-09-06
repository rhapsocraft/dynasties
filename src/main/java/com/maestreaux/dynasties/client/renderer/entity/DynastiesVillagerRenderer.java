package com.maestreaux.dynasties.client.renderer.entity;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.client.model.DynastiesVillagerModel;
import com.maestreaux.dynasties.client.renderer.entity.state.DynastiesVillagerRenderState;
import com.maestreaux.dynasties.world.entities.DynastiesVillager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CreakingRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class DynastiesVillagerRenderer extends MobRenderer<DynastiesVillager, DynastiesVillagerRenderState, DynastiesVillagerModel> {
    private static final ResourceLocation VILLAGER_BASE_SKIN = ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, "textures/entity/villager.png");

    public DynastiesVillagerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new DynastiesVillagerModel(ctx.bakeLayer(DynastiesVillagerModel.LAYER_LOCATION)), 0.5F);
        // this.addLayer(new CustomHeadLayer<>(this, p_174437_.getModelSet(), p_174437_.getItemInHandRenderer()));
        // this.addLayer(new VillagerProfessionLayer(this, p_174437_.getResourceManager(), "villager"));
        // this.addLayer(new CrossedArmsItemLayer(this, p_174437_.getItemInHandRenderer()));
    }

    public void extractRenderState(DynastiesVillager villager, DynastiesVillagerRenderState renderState, float p_368483_) {
        super.extractRenderState(villager, renderState, p_368483_);
        renderState.fleeAnimationState.copyFrom(villager.fleeAnimationState);
        renderState.idleAnimationState.copyFrom(villager.idleAnimationState);
        renderState.idleFaceAnimationState.copyFrom(villager.idleFaceAnimationState);
        renderState.isFleeing = villager.isFleeing();
    }

    @Override
    public @NotNull DynastiesVillagerRenderState createRenderState() {
        return new DynastiesVillagerRenderState();
    }

    @Override
    protected void scale(DynastiesVillagerRenderState renderState, PoseStack pPoseStack) {
        float $$3 = 0.9375F;

//        if (entity.isBaby()) {
//            $$3 *= 0.5F;
//            this.shadowRadius = 0.25F;
//        } else {
//            this.shadowRadius = 0.5F;
//        }

        pPoseStack.scale($$3, $$3, $$3);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull DynastiesVillagerRenderState dynastiesVillagerRenderState) {
        return VILLAGER_BASE_SKIN;
    }
}
