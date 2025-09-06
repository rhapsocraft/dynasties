package com.maestreaux.dynasties.client.model;// Made with Blockbench 4.10.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.maestreaux.dynasties.DynastiesMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TentModel extends Model {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, "tent"), "main");
	private final ModelPart tent;

	public TentModel(ModelPart root) {
		super(root, RenderType::entityCutoutNoCull);
		this.tent = root.getChild("tent");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition tent = partdefinition.addOrReplaceChild("tent", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -1.0F, -11.0F, 16.0F, 1.0F, 32.0F, new CubeDeformation(0.0F))
				.texOffs(67, 0).addBox(-6.0F, -2.0F, 13.0F, 12.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r1 = tent.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 10).addBox(-2.0F, -2.0F, -2.5F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.25F, -23.0F, -2.5F, -0.0873F, 0.0F, 0.0F));

		PartDefinition cube_r2 = tent.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 10).addBox(-2.0F, -2.0F, -2.5F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.25F, -21.5F, 15.5F, -0.0873F, 0.0F, 0.0F));

		PartDefinition cube_r3 = tent.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(2, 2).addBox(-5.0F, -3.5F, 0.0F, 5.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.75F, -22.5F, 0.0F, -0.3927F, -0.3927F, 0.0F));

		PartDefinition cube_r4 = tent.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -13.0F, -16.0F, 0.0F, 26.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.6119F, -10.9698F, 6.0F, 0.0F, -0.0436F, -0.5672F));

		PartDefinition cube_r5 = tent.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(78, 61).addBox(0.5F, -12.0F, -1.5F, 0.0F, 25.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.75F, -11.5F, -13.0F, -0.4754F, -0.7268F, 0.3295F));

		PartDefinition cube_r6 = tent.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(78, 61).addBox(0.5F, -12.0F, -1.5F, 0.0F, 25.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.75F, -11.5F, -13.0F, -0.4754F, 0.7268F, -0.3295F));

		PartDefinition cube_r7 = tent.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(8, 99).mirror().addBox(-10.5F, -12.5F, 0.0F, 21.0F, 25.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.4246F, -9.0632F, 21.95F, 0.0F, -0.1309F, 0.5672F));

		PartDefinition cube_r8 = tent.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(8, 99).addBox(-10.5F, -12.5F, 0.0F, 21.0F, 25.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.9246F, -9.0632F, 21.55F, 0.0F, 0.1309F, -0.5672F));

		PartDefinition cube_r9 = tent.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(27, 53).addBox(-1.5F, -1.5F, -20.5F, 3.0F, 3.0F, 37.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.25F, -22.0F, 8.0F, -0.0873F, 0.0F, 0.0F));

		PartDefinition cube_r10 = tent.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 32).addBox(0.0F, -13.0F, -16.0F, 0.0F, 26.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.1119F, -10.9698F, 6.0F, 0.0F, 0.0436F, 0.5672F));

		PartDefinition supp_rear = tent.addOrReplaceChild("supp_rear", CubeListBuilder.create(), PartPose.offset(-9.0F, -10.0F, 24.0F));

		PartDefinition cube_r11 = supp_rear.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(82, 33).addBox(-1.0F, -15.5F, 0.0F, 2.0F, 31.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6119F, -2.5302F, -2.5F, 0.0F, -0.6109F, 0.5672F));

		PartDefinition cube_r12 = supp_rear.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(78, 33).addBox(-1.0F, -15.5F, 0.0F, 2.0F, 31.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.1381F, -2.5302F, -2.75F, 0.0F, 0.6109F, -0.5672F));

		PartDefinition supp_front = tent.addOrReplaceChild("supp_front", CubeListBuilder.create(), PartPose.offset(-0.25F, -24.9519F, -8.125F));

		PartDefinition cube_r13 = supp_front.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(70, 33).addBox(-1.0F, -17.0F, 0.0F, 2.0F, 34.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.8695F, 11.0F, -1.125F, 0.0F, -0.6109F, -0.5672F));

		PartDefinition cube_r14 = supp_front.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(74, 33).addBox(-1.0F, -17.0F, 0.0F, 2.0F, 34.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.8695F, 11.0F, -0.875F, 0.0F, 0.6109F, 0.5672F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}
}