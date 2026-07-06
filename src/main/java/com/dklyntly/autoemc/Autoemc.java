package com.dklyntly.autoemc;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * AutoEMC
 *
 * Scans recipes after ProjectE finishes its normal EMC mapping and derives EMC
 * values from ingredient costs for items ProjectE did not manage to value.
 * Fallback values are enabled by default; extreme mod ids can use trillion-scale fallback values.
 */
@Mod(AutoEMC.MODID)
public class AutoEMC {

    public static final String MODID = "autoemc";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public AutoEMC() {
        net.minecraftforge.fml.ModLoadingContext.get()
                .registerConfig(ModConfig.Type.COMMON, AutoEMCConfig.COMMON_SPEC);

        MinecraftForge.EVENT_BUS.register(EMCAutoFiller.class);
    }
}
