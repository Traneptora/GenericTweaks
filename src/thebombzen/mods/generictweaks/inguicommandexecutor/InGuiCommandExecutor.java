package thebombzen.mods.generictweaks.inguicommandexecutor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid="generictweaks.inguicommandexecutor", name="In-Gui Command Executor")
@SideOnly(Side.CLIENT)
public class InGuiCommandExecutor {
	
	public Map<Integer, String> keys = new HashMap<Integer, String>();
	public Map<Integer, Boolean> prevDown = new HashMap<Integer, Boolean>();
	private long keysLastModified = -1L;
	private File keysFile;
	
	@SubscribeEvent
	public void clientTickEvent(ClientTickEvent event) throws IOException {
		if (!event.phase.equals(Phase.START)){
			return;
		}
		if (keysFile.lastModified() != keysLastModified){
			loadKeysFile();
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Reloaded in-gui commands list."));
		}
		for (int key : keys.keySet()){
			if (!prevDown.get(key) && Keyboard.isKeyDown(key)){
				Minecraft.getMinecraft().thePlayer.sendChatMessage(keys.get(key));
			}
			prevDown.put(key, Keyboard.isKeyDown(key));
		}
	}
	
	private void loadKeysFile() throws IOException {
		keysFile = new File(new File(Minecraft.getMinecraft().mcDataDir, "config"), "gt_keys.txt");
		if (!keysFile.exists()){
			keysFile.createNewFile();
		}
		BufferedReader br = new BufferedReader(new FileReader(keysFile));
		String line = null;
		while (null != (line = br.readLine())){
			int index = line.indexOf('=');
			if (index < 0){
				continue;
			}
			String key = line.substring(0, index);
			String value = line.substring(index + 1);
			int keyCode = Keyboard.getKeyIndex(key.trim().toUpperCase()); 
			keys.put(keyCode, value.trim());
			prevDown.put(keyCode, false);
		}
		br.close();
		keysLastModified = keysFile.lastModified();
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) throws IOException {
		FMLCommonHandler.instance().findContainerFor(this).getMetadata().authorList = Arrays.asList("Thebombzen");
		FMLCommonHandler.instance().bus().register(this);
		loadKeysFile();
	}
	
}
