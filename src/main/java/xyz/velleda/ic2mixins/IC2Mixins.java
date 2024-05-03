package xyz.velleda.ic2mixins;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = IC2Mixins.MOD_ID)
public class IC2Mixins {
    public static final String MOD_ID = "ic2-mixins";

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        System.out.println("IC2 mixins loaded!");
    }
}
