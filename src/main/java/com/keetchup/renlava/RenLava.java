package com.keetchup.renlava;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.keetchup.renlava.common.block.ModBlocks.BLOCKITEMS;
import static com.keetchup.renlava.common.block.ModBlocks.BLOCKS;

@Mod(RenLava.MODID)
public class RenLava {

    public static final String MODID = "renlava";

    public RenLava() {
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCKITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RenLavaConfig.SPEC);

        RenLavaEvent eventHandle = new RenLavaEvent();
        MinecraftForge.EVENT_BUS.register(eventHandle);

        MinecraftForge.EVENT_BUS.register(this);
    }

    /*private void setup(final FMLCommonSetupEvent event) {
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
    }

    private void processIMC(final InterModProcessEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
    }*/
}
