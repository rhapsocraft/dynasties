package com.maestreaux.dynasties;

import com.maestreaux.dynasties.network.PacketHandler;
import com.maestreaux.dynasties.world.Zone;
import com.maestreaux.dynasties.world.entities.DynastiesVillager;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import com.maestreaux.dynasties.init.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(DynastiesMod.MODID)
public class DynastiesMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "villagerdynasties";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace

    public DynastiesMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModItems.CREATIVE_MODE_TABS.register(modEventBus);
        ModEntityDataSerializers.ENTITY_DATA_SERIALIZERS.register(modEventBus);
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModSensorTypes.SENSOR_TYPES.register(modEventBus);
        ModMemoryTypes.MEMORY_TYPES.register(modEventBus);

        // Custom Registries
        ModBuildings.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(DynastiesMod.class);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));

        event.enqueueWork(PacketHandler::register);
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
    }

    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide()) {
            var serverLevel = (ServerLevel) event.getLevel();

            if (event.getItemStack().is(ModItems.DEBUG_TOOL.get())) {
                var hitPos = event.getHitVec().getBlockPos();
                var level = event.getLevel();
                var position = level.getBlockState(hitPos).isSuffocating(level, hitPos) ?  hitPos.above() : hitPos;

                var newZone = new Zone(serverLevel, position);

                Zone.add(serverLevel, newZone);

                for(int i = 0; i < 1; i++) {
                    var newVillager = new DynastiesVillager(serverLevel, newZone);
                    serverLevel.addFreshEntity(newVillager);
                    newVillager.moveTo(newZone.getCenter().above().getCenter());
                }

            } else if (event.getItemStack().is(Items.STICK)) {
                Zone.getZones(serverLevel);
            }
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
