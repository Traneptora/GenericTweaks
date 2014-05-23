package thebombzen.mods.generictweaks.fastmining;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;

import thebombzen.mods.generictweaks.ReflectionHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod(modid="generictweaks.fastmining", name="GT Fast Mining", version="1.0")
public class FastMining {
	@EventHandler
	public void preInit(FMLPreInitializationEvent event){
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public static final String[] currentBlockXStrings = { "currentBlockX", "field_78775_c", "c"};
	public static final String[] currentBlockYStrings = { "currentBlockY", "field_78772_d", "d"};
	public static final String[] currentBlockZStrings = { "currentblockZ", "field_78773_e", "e"};
	public static final String[] curBlockDamageMPStrings = { "curBlockDamageMP", "field_78770_f", "f"};
	public static final String[] isHittingBlockStrings = { "isHittingBlock", "field_78778_j", "j"};
	public static final String[] blockHitDelayStrings = { "blockHitDelay", "field_78781_i", "i"};
	

	private int blockX;
	private int blockY;
	private int blockZ;
	private boolean enabled = false;
	
	@SubscribeEvent
	public void keyboardEvent(InputEvent.KeyInputEvent event){
		if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD5) && !Keyboard.isRepeatEvent()){
			enabled = !enabled;
			if (enabled){
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("FastMining enabled."));
			} else {
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("FastMining disabled."));
			}
		}
	}
	
	@SubscribeEvent
	public void clientTick(ClientTickEvent event){
		if (!enabled){
			return;
		}
		if (event.phase != Phase.END){
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.theWorld == null){
			return;
		}
		if ((Integer)ReflectionHelper.getPrivateField(mc.playerController, PlayerControllerMP.class, blockHitDelayStrings) > 1){
			ReflectionHelper.setPrivateField(mc.playerController, PlayerControllerMP.class, 1, blockHitDelayStrings);
		}
		blockX = ReflectionHelper.getPrivateField(mc.playerController, PlayerControllerMP.class, currentBlockXStrings);
		blockY = ReflectionHelper.getPrivateField(mc.playerController, PlayerControllerMP.class, currentBlockYStrings);
		blockZ = ReflectionHelper.getPrivateField(mc.playerController, PlayerControllerMP.class, currentBlockZStrings);
		if (blockY < 0){
			return;
		}
		Block block = mc.theWorld.getBlock(blockX, blockY, blockZ);
		if (block.isAir(mc.theWorld, blockX, blockY, blockZ)){
			return;
		}
		float str = block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, blockX, blockY, blockZ);
		if (str >= 1.0F){
			return;
		}
		boolean isHittingBlock = ReflectionHelper.getPrivateField(mc.playerController, PlayerControllerMP.class, isHittingBlockStrings);
		if (!isHittingBlock){
			return;
		}
		//float damage = ReflectionHelper.getPrivateField(mc.playerController, PlayerControllerMP.class, curBlockDamageMPStrings);
		int side = mc.objectMouseOver.sideHit;
		mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(1, blockX, blockY, blockZ, side));
		mc.playerController.onPlayerDamageBlock(blockX, blockY, blockZ, side);
		boolean newIsHittingBlock = ReflectionHelper.getPrivateField(mc.playerController, PlayerControllerMP.class, isHittingBlockStrings);
		if (newIsHittingBlock){
			mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(1, blockX, blockY, blockZ, side));
		} else {
			ReflectionHelper.setPrivateField(mc.playerController, PlayerControllerMP.class, 1, blockHitDelayStrings);
		}
		//float newDamage = ReflectionHelper.getPrivateField(mc.playerController, PlayerControllerMP.class, curBlockDamageMPStrings);
	}
	
}
