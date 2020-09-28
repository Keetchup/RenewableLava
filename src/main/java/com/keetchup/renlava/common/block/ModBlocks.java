package com.keetchup.renlava.common.block;

import com.keetchup.renlava.RenLava;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, RenLava.MODID);
    public static final DeferredRegister<Item> BLOCKITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RenLava.MODID);

    public static final RegistryObject<Block> CRUCIBLE_BLOCK = BLOCKS.register("crucible", () -> new CrucibleBlock(Block.Properties.create(Material.IRON, MaterialColor.STONE).hardnessAndResistance(0.4F).notSolid()));
    public static final RegistryObject<BlockItem> CRUCIBLE_BLOCKITEM = BLOCKITEMS.register("crucible", ()->new BlockItem(CRUCIBLE_BLOCK.get(), new Item.Properties().group(ItemGroup.BREWING)));
}
