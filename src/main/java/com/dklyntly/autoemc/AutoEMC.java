package com.dklyntly.autoemc;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = AutoEMC.MODID,
        name = AutoEMC.NAME,
        version = AutoEMC.VERSION,
        acceptableRemoteVersions = "*",
        dependencies = "after:projecte"
)
public class AutoEMC {

    public static final String MODID = "autoemc";
    public static final String NAME = "AutoEMC Lite";
    public static final String VERSION = "1.0.0-1.12.2";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        AutoEMCConfig.load(event.getSuggestedConfigurationFile());
        AutoEMCFileLogger.configure(event.getModConfigurationDirectory());
        MinecraftForge.EVENT_BUS.register(EMCAutoFiller.class);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandAutoEMC());
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        // Queue instead of scanning immediately. In 1.12.2 some packs finish
        // recipe/OreDictionary/ProjectE setup at or just after world load.
        EMCAutoFiller.queueAutomaticScan("server/world load");
    }
}
