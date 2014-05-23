package thebombzen.mods.generictweaks.autotorch;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;

@Mod(modid="generictweaks.autotorch", name="GT AutoTorch", version="1.0")
public class AutoTorch {
	@EventHandler
	public void preInit(FMLPreInitializationEvent event){
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private boolean enabled = false;
	
	@SubscribeEvent
	public void keyboardEvent(KeyInputEvent event){
		if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD0) && !Keyboard.isRepeatEvent()){
			enabled = !enabled;
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(("GT AutoTorch " + (enabled ? "Enabled." : "Disabled."))));
		}
	}
	
	@SubscribeEvent
	public void clientTick(ClientTickEvent event){
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.theWorld == null){
			return;
		}
		if (!enabled){
			return;
		}
		int x = (int)(mc.thePlayer.posX - 0.5D);
		int y = (int)mc.thePlayer.boundingBox.minY;
		int z = (int)(mc.thePlayer.posZ - 0.5D);
		int value = mc.theWorld.getSavedLightValue(EnumSkyBlock.Block, x, y, z);
		//System.out.println(value);
		if (value < 3 && mc.theWorld.getBlock(x, y, z).isReplaceable(mc.theWorld, x, y, z)){
			for (int i = 0; i < 9; i++){
				ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
				if (stack == null || !stack.getItem().equals(Item.getItemFromBlock(Blocks.torch))){
					continue;
				}
				int prev = mc.thePlayer.inventory.currentItem;
				mc.thePlayer.inventory.currentItem = i;
				float pitch = mc.thePlayer.rotationPitch;
				mc.thePlayer.rotationPitch = 90F;
				Minecraft.getMinecraft().entityRenderer.getMouseOver(1.0F);
				mc.playerController.onPlayerRightClick(mc.thePlayer, mc.thePlayer.worldObj, mc.thePlayer.inventory.mainInventory[i], mc.objectMouseOver.blockX, mc.objectMouseOver.blockY, mc.objectMouseOver.blockZ, mc.objectMouseOver.sideHit, mc.objectMouseOver.hitVec);
				mc.thePlayer.rotationPitch = pitch;
				Minecraft.getMinecraft().entityRenderer.getMouseOver(1.0F);
				mc.thePlayer.inventory.currentItem = prev;
				break;
			}
		}
	}
}
