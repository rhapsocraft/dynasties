package com.maestreaux.dynasties.world;

import com.maestreaux.dynasties.DynastiesMod;
import com.maestreaux.dynasties.init.ModBuildings;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.IOException;

public class Building {
    private final ResourceLocation blueprint;
    private final String name;
    private StructureTemplate template;

    public Building(String name, String resourcePath) {
        this.name = name;
        this.blueprint = ResourceLocation.fromNamespaceAndPath(DynastiesMod.MODID, resourcePath);
    }

    public String getName() {
        return this.name;
    }

    public StructureTemplate getTemplate() {
        return this.template;
    }

    public void loadTemplate(MinecraftServer server) {
        var resource = server.getResourceManager().getResource(this.blueprint);

        if (resource.isPresent()) {
            try {
                var buildingTag = NbtIo.readCompressed(resource.get().open(), NbtAccounter.unlimitedHeap());
                var newTemplate = new StructureTemplate();
                newTemplate.load(server.registryAccess().lookupOrThrow(Registries.BLOCK), buildingTag);

                this.template = newTemplate;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
