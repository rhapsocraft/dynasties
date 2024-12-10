package com.maestreaux.dynasties.client.renderer;

import com.maestreaux.dynasties.world.entities.DynastyVillager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DynastyVillagerRenderer extends MobRenderer<DynastyVillager, VillagerModel<DynastyVillager>> {
    private static final ResourceLocation VILLAGER_BASE_SKIN = new ResourceLocation("textures/entity/villager/villager.png");

    public DynastyVillagerRenderer(EntityRendererProvider.Context p_174437_) {
        super(p_174437_, new VillagerModel<>(p_174437_.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
        this.addLayer(new CustomHeadLayer<>(this, p_174437_.getModelSet(), p_174437_.getItemInHandRenderer()));
        this.addLayer(new VillagerProfessionLayer(this, p_174437_.getResourceManager(), "villager"));
        this.addLayer(new CrossedArmsItemLayer(this, p_174437_.getItemInHandRenderer()));
    }

    public ResourceLocation getTextureLocation(DynastyVillager pEntity) {
        return VILLAGER_BASE_SKIN;
    }

    protected void scale(DynastyVillager pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
        float $$3 = 0.9375F;
        if (pLivingEntity.isBaby()) {
            $$3 *= 0.5F;
            this.shadowRadius = 0.25F;
        } else {
            this.shadowRadius = 0.5F;
        }

        pPoseStack.scale($$3, $$3, $$3);
    }
}
