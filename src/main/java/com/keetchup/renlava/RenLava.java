package com.keetchup.renlava;

import com.keetchup.renlava.config.RenLavaConfig;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RenLava implements ModInitializer {

    public static final CrucibleBlock CRUCIBLE = new CrucibleBlock(FabricBlockSettings.of(Material.METAL, MaterialColor.STONE).strength(2.0F).nonOpaque());

    public static RenLavaConfig CONFIG;

    @Override
    public void onInitialize() {
        AutoConfig.register(RenLavaConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(RenLavaConfig.class).getConfig();

        Registry.register(Registry.BLOCK, new Identifier("renlava", "crucible"), CRUCIBLE);
        Registry.register(Registry.ITEM, new Identifier("renlava", "crucible"), new BlockItem(CRUCIBLE, new Item.Settings().group(ItemGroup.BREWING)));

    }

}
