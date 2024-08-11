package com.maestreaux.dynasties.init;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.items.wrapper.InvWrapper;

public class ModCapabilities {
    public static final Capability<InvWrapper> INVENTORY_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
}
