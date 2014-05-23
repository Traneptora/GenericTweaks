package thebombzen.mods.generictweaks.enderchest;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@Mod(modid="generictweaks.betterenderchests", name="GT Enderchests", version="1.0")
public class BetterEnderchests {
	@EventHandler public void preInit(FMLPreInitializationEvent event){
		MinecraftForge.EVENT_BUS.register(this);
	}
	@SubscribeEvent public void harvestDrops(HarvestDropsEvent event){
		if (Blocks.ender_chest.equals(event.block)){
			event.drops.clear();
			event.drops.add(new ItemStack(Blocks.ender_chest));
		}
	}
}
