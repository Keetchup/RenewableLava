package com.keetchup.renlava;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RenLava implements ModInitializer {

    public static final CrucibleBlock CRUCIBLE = new CrucibleBlock(FabricBlockSettings.of(Material.METAL));

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier("renlava", "crucible"), CRUCIBLE);
        Registry.register(Registry.ITEM, new Identifier("renlava", "crucible"), new BlockItem(CRUCIBLE, new Item.Settings().group(ItemGroup.BREWING)));
    }
}
