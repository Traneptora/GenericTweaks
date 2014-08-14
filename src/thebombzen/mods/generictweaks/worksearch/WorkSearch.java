package thebombzen.mods.generictweaks.worksearch;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.ClientCommandHandler;
import thebombzen.mods.generictweaks.ReflectionHelper;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid="generictweaks.worksearch", name="GT WorkSearch", version="1.0")
public class WorkSearch {
	
	public static final Minecraft mc = Minecraft.getMinecraft();
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event){
		//FMLCommonHandler.instance().bus().register(this);
		ClientCommandHandler.instance.registerCommand(new CommandBase(){

			@Override
			public String getCommandName() {
				return "worksearch";
			}

			@Override
			public String getCommandUsage(ICommandSender sender) {
				return "Usage: /worksearch";
			}

			@Override
			public void processCommand(ICommandSender sender,
					String[] args) {
				workSearch();
			}
			
			@Override
			public boolean canCommandSenderUseCommand(ICommandSender sender){
				return true;
			}
			
		});
	}
	
	
	
	public void workSearch(){
		List<int[]> workbenchCoordinates = new ArrayList<int[]>();
		List<Chunk> chunks = ReflectionHelper.getPrivateField((ChunkProviderClient)mc.theWorld.getChunkProvider(), ChunkProviderClient.class, "chunkListing", "field_73237_c", "c");
		for (Chunk chunk : chunks){
			for (int x = 0; x < 16; x++){
				for (int y = 0; y < 256; y++){
					for (int z = 0; z < 16; z++){
						if (chunk.getBlock(x, y, z).equals(Blocks.crafting_table)){
							workbenchCoordinates.add(new int[]{(chunk.xPosition << 4) | x, y, (chunk.zPosition << 4) | z});
						}
					}
				}
			}
		}
		if (workbenchCoordinates.isEmpty()){
			mc.thePlayer.addChatMessage(new ChatComponentText("No workbenches found."));
		} else {
			for (int[] coords : workbenchCoordinates){
				mc.thePlayer.addChatMessage(new ChatComponentText(String.format("Workbench: %d,  %d, %d.", coords[0], coords[1], coords[2])));
			}
		}
		
	}
}
