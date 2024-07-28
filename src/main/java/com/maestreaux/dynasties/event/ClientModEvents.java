package com.maestreaux.dynasties.event;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.client.model.TentModel;
import com.maestreaux.dynasties.client.renderer.DynastyVillagerRenderer;
import com.maestreaux.dynasties.client.renderer.TentRenderer;
import com.maestreaux.dynasties.init.ModBlockEntityTypes;
import com.maestreaux.dynasties.init.ModEntityTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DynastiesMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.DYNASTY_VILLAGER.get(), DynastyVillagerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.TENT_BE.get(), TentRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TentModel.LAYER_LOCATION, TentModel::createBodyLayer);
    }
}
