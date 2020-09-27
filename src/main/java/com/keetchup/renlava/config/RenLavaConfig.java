package com.keetchup.renlava.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

import static com.keetchup.renlava.RenLava.CONFIG;


@Config(name = "renlava")
public class RenLavaConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip(count = 2)
    public String primaryResource = "minecraft:obsidian";

    @ConfigEntry.BoundedDiscrete(min = 1, max = 4)
    @ConfigEntry.Gui.Tooltip(count = 2)
    public int primaryResourceModifier = 1;

    @ConfigEntry.Gui.Tooltip(count = 2)
    public String secondaryResource = "minecraft:blaze_powder";

    @ConfigEntry.BoundedDiscrete(min = 1, max = 16)
    @ConfigEntry.Gui.Tooltip(count = 2)
    public int secondaryResourceModifier = 1;

    @ConfigEntry.Gui.Tooltip(count = 2)
    public float lavaConvertChanceModifier = 1;

    public static int getPrimaryConfigModifier() {
        return CONFIG.primaryResourceModifier;
    }

    public static int getSecondaryConfigModifier() {
        return CONFIG.secondaryResourceModifier;
    }

    public static float getLavaConvertChanceModifier(){
        return (CONFIG.lavaConvertChanceModifier == 0) ? 1 : CONFIG.lavaConvertChanceModifier;
    }

    public static Item getPrimaryResource(){
        Item item = Registry.ITEM.get(new Identifier(CONFIG.primaryResource));
        return item == Items.AIR ? Items.OBSIDIAN : item;
    }

    public static Item getSecondaryResource(){
        Item item = Registry.ITEM.get(new Identifier(CONFIG.secondaryResource));
        return item == Items.AIR ? Items.BLAZE_POWDER : item;
    }

}



