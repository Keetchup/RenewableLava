package com.keetchup.renlava.config;

import com.keetchup.renlava.RenLava;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;


@Config(name = "renlava")
public class RenLavaConfig implements ConfigData {

    @ConfigEntry.BoundedDiscrete(min = 1, max = 4)
    @ConfigEntry.Gui.Tooltip
    public int obsidianModifier = 1;

    @ConfigEntry.BoundedDiscrete(min = 1, max = 16)
    @ConfigEntry.Gui.Tooltip
    public int blazePowderModifier = 1;

    @ConfigEntry.Gui.Tooltip
    public float lavaConvertChanceModifier = 1;

    public static int getObsidianModifier() {
        return RenLava.CONFIG.obsidianModifier;
    }

    public static int getBlazePowderModifier() {
        return RenLava.CONFIG.blazePowderModifier;
    }

    public static float getLavaConvertChanceModifier(){
        return (RenLava.CONFIG.lavaConvertChanceModifier == 0) ? 1 : RenLava.CONFIG.lavaConvertChanceModifier;
    }
}



