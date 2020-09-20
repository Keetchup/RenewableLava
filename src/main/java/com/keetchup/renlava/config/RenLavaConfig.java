package com.keetchup.renlava.config;

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

}



