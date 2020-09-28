package com.keetchup.renlava;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class RenLavaConfig {

    public static final ForgeConfigSpec SPEC;
    public static final RenLavaConfig RENLAVACONFIG;

    static {
        final Pair<RenLavaConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(RenLavaConfig::new);
        SPEC = specPair.getRight();
        RENLAVACONFIG = specPair.getLeft();
    }

    public final ForgeConfigSpec.IntValue obsidianModifier;
    public final ForgeConfigSpec.IntValue blazePowderModifier;
    public final ForgeConfigSpec.DoubleValue lavaModifier;


    public RenLavaConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Renewable Lava Configuration");
        builder.push("general");

        obsidianModifier = builder.comment("Number of \"stacks\" each item of Obsidian adds. Crucible requires 4 \"stack\" to fill up").defineInRange("obsidianModifier", 1, 1, 4);
        blazePowderModifier = builder.comment("Number of \"stacks\" each item of Blaze Powder adds. Crucible requires 16 \"stack\" to fill up").defineInRange("blazePowderModifier", 1, 1, 16);
        lavaModifier = builder.comment("Percentage of chance of material converting into lava inside of Crucible").defineInRange("lavaConvertingModifier", 1.0, Double.MIN_VALUE, Double.MAX_VALUE);
    }


}
