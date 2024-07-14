package com.maestreaux.dynasties.world;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.IOException;
import java.util.List;

public class Building {
    private final ResourceLocation blueprint;
    private final String name;
    private StructureTemplate template;

    public Building(String name, String resourcePath) {
        this.name = name;
        this.blueprint = new ResourceLocation("villagerdynasties", resourcePath);
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
                var buildingTag = NbtIo.readCompressed(resource.get().open());
                var newTemplate = new StructureTemplate();
                newTemplate.load(BuiltInRegistries.BLOCK.asLookup(), buildingTag);

                this.template = newTemplate;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
