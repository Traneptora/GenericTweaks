package thebombzen.mods.generictweaks.fastcrafting;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.network.play.client.C01PacketChatMessage;
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

@Mod(modid = "generictweaks.fastcrafting", name = "GT Fast Crafting", version = "1.0")
@SideOnly(Side.CLIENT)
public class FastCrafting {
	private List<Recipe> recipes = new ArrayList<Recipe>();
	private Map<String, String> commands = new HashMap<String, String>();
	private Map<Integer, Boolean> previouslyDown = new HashMap<Integer, Boolean>();
	private File recipesFile = new File(new File(
			Minecraft.getMinecraft().mcDataDir, "config"), "gt_recipes.dat");
	private File commandsFile = new File(new File(
			Minecraft.getMinecraft().mcDataDir, "config"),
			"gt_recipes_commands.txt");
	private long commandsLastModified = -1L;
	private boolean shouldCraft = false;
	private long sleeptime = 0;
	private int stage = 0;
	private Recipe recipe;

	private void loadRecipesFile() throws IOException {
		if (!recipesFile.exists()) {
			return;
		}
		DataInputStream din = new DataInputStream(new FileInputStream(
				recipesFile));
		recipes.clear();
		int size = din.readInt();
		for (int i = 0; i < size; i++) {
			recipes.add(new Recipe(din));
		}
		din.close();
	}

	private void saveRecipesFile() throws IOException {
		DataOutputStream dout = new DataOutputStream(new FileOutputStream(
				recipesFile));
		dout.writeInt(recipes.size());
		for (Recipe recipe : recipes) {
			recipe.writeToOutputStream(dout);
		}
		dout.close();
	}

	private void loadCommandsFile() throws IOException {
		if (!commandsFile.exists()) {
			commandsFile.createNewFile();
		}
		commands.clear();
		BufferedReader br = new BufferedReader(new FileReader(commandsFile));
		String line = null;
		while (null != (line = br.readLine())) {
			int index = line.indexOf('=');
			if (index < 0) {
				continue;
			}
			String itemname = line.substring(0, index);
			String command = line.substring(index + 1);
			commands.put(itemname, command);
		}
		br.close();
		commandsLastModified = commandsFile.lastModified();
	}

	@SubscribeEvent
	public void clientTickEvent(ClientTickEvent event) throws IOException {
		if (!event.phase.equals(Phase.START)){
			return;
		}
		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiCrafting)) {
			return;
		}
		GuiCrafting craftScreen = (GuiCrafting) Minecraft.getMinecraft().currentScreen;
		ContainerWorkbench container = (ContainerWorkbench) craftScreen.inventorySlots;
		if (sleeptime > 0){
			sleeptime--;
			if (sleeptime == 0) {
				ReducedItemStack result = recipe.getResult();
				switch (stage) {
				case 1:
					Minecraft.getMinecraft().playerController.windowClick(
							container.windowId, container.getSlotFromInventory(
									container.craftResult, 0).slotNumber, 0, 1,
							Minecraft.getMinecraft().thePlayer);
					sleeptime = 1;
					stage = 2;
					return;
				case 2:
					if (commands.containsKey(result.getName())) {
						Minecraft.getMinecraft().thePlayer.sendQueue
								.addToSendQueue(new C01PacketChatMessage(
										commands.get(result.getName())));
						sleeptime = 10;
						stage = 3;
						return;
					}
				case 3:
					for (int i = 0; i < 9; i++) {
						Minecraft.getMinecraft().playerController.windowClick(
								container.windowId,
								container.getSlotFromInventory(
										container.craftMatrix, i).slotNumber,
								0, 1, Minecraft.getMinecraft().thePlayer);
					}
					stage = 0;
					return;
				}
				
			}
			return;
		}
		if (commandsLastModified != commandsFile.lastModified()) {
			loadCommandsFile();
			Minecraft.getMinecraft().thePlayer
					.addChatMessage(new ChatComponentText(
							"Reloaded fast crafting commands."));
		}
		if (shouldCraft){
			boolean success = craftOne(craftScreen, container);
			if (!success){
				shouldCraft = false;
			}
			return;
		}
		boolean mDown = Keyboard.isKeyDown(Keyboard.KEY_M)
				&& !previouslyDown.get(Keyboard.KEY_M);
		previouslyDown.put(Keyboard.KEY_M, Keyboard.isKeyDown(Keyboard.KEY_M));
		boolean nDown = Keyboard.isKeyDown(Keyboard.KEY_N)
				&& !previouslyDown.get(Keyboard.KEY_N);
		previouslyDown.put(Keyboard.KEY_N, Keyboard.isKeyDown(Keyboard.KEY_N));
		boolean bDown = Keyboard.isKeyDown(Keyboard.KEY_B)
				&& !previouslyDown.get(Keyboard.KEY_B);
		previouslyDown.put(Keyboard.KEY_B, Keyboard.isKeyDown(Keyboard.KEY_B));
		boolean cDown = Keyboard.isKeyDown(Keyboard.KEY_C)
				&& !previouslyDown.get(Keyboard.KEY_C);
		previouslyDown.put(Keyboard.KEY_C, Keyboard.isKeyDown(Keyboard.KEY_C));
		if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && mDown) {
			recipes.clear();
			saveRecipesFile();
			Minecraft.getMinecraft().thePlayer
					.addChatMessage(new ChatComponentText("Cleared Recipe List"));
			return;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && (nDown ^ bDown)) {
			List<ReducedItemStack> items = new ArrayList<ReducedItemStack>();
			for (int i = 0; i < 9; i++) {
				items.add(new ReducedItemStack(container.craftMatrix
						.getStackInSlot(i), nDown));
			}
			ItemStack result = CraftingManager.getInstance()
					.findMatchingRecipe(container.craftMatrix,
							Minecraft.getMinecraft().theWorld);
			Recipe recipe = new Recipe(nDown, new ReducedItemStack(result, nDown), items);
			recipes.add(recipe);
			saveRecipesFile();
			Minecraft.getMinecraft().thePlayer
					.addChatMessage(new ChatComponentText("Added " + (nDown ? "Specific" : "Nonspecific") + " Recipe: "
							+ recipe));
			return;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && cDown) {
			boolean success = craftOne(craftScreen, container);
			if (success && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
				shouldCraft = true;
			}
			return;
		}
	}

	private boolean doesHaveEnough(List<Slot> slots, int amount) {
		for (Slot slot : slots) {
			amount -= slot.getStack().stackSize;
			if (amount <= 0) {
				return true;
			}
		}
		return false;
	}

	private boolean craftOne(GuiCrafting crafting, ContainerWorkbench container) {
		if (recipes.size() == 0) {
			return false;
		}
		recipelist: for (int recipeNum = recipes.size() - 1; recipeNum >= 0; recipeNum--) {
			recipe = recipes.get(recipeNum);
			ReducedItemStack[] stacks = recipe.getItemStacks();
			Map<ReducedItemStack, List<Slot>> requiredIn = new HashMap<ReducedItemStack, List<Slot>>();
			Map<ReducedItemStack, List<Slot>> presentIn = new HashMap<ReducedItemStack, List<Slot>>();
			Map<ReducedItemStack, Boolean> drag = new HashMap<ReducedItemStack, Boolean>();
			for (int craftSlot = 0; craftSlot < 9; craftSlot++) {
				ReducedItemStack stack = stacks[craftSlot];
				if (stack.isEmpty()) {
					continue;
				}
				if (requiredIn.containsKey(stack)) {
					requiredIn.get(stack).add(
							container.getSlotFromInventory(
									container.craftMatrix, craftSlot));
				} else {
					List<Slot> slotsList = new ArrayList<Slot>();
					slotsList.add(container.getSlotFromInventory(
							container.craftMatrix, craftSlot));
					requiredIn.put(stack, slotsList);
				}
			}
			for (int invSlot = 0; invSlot < container.inventorySlots.size(); invSlot++) {
				Slot slot = (Slot) container.inventorySlots.get(invSlot);
				ReducedItemStack stack = new ReducedItemStack(slot.getStack(), recipe.isSpecific());
				if (stack.isEmpty()){
					continue;
				}
				if (presentIn.containsKey(stack)) {
					presentIn.get(stack).add(
							(Slot) container.inventorySlots.get(invSlot));
				} else {
					List<Slot> slotsList = new ArrayList<Slot>();
					slotsList.add((Slot) container.inventorySlots.get(invSlot));
					presentIn.put(stack, slotsList);
				}
			}
			for (ReducedItemStack stack : requiredIn.keySet()) {
				if (stack.isEmpty()) {
					continue;
				}
				if (!presentIn.containsKey(stack)){
					continue recipelist;
				}
				List<Slot> required = requiredIn.get(stack);
				List<Slot> present = presentIn.get(stack);
				if (present.size() >= required.size()) {
					drag.put(stack, false);
				} else {
					if (!doesHaveEnough(present, required.size())) {
						continue recipelist;
					}
					drag.put(stack, true);
				}
			}
			for (ReducedItemStack stack : requiredIn.keySet()) {
				List<Slot> required = requiredIn.get(stack);
				List<Slot> present = presentIn.get(stack);
				if (drag.get(stack)) { 
					for (int i = 0; i < present.size(); i++) {
						Minecraft.getMinecraft().playerController.windowClick(
								container.windowId, present.get(i).slotNumber,
								0, 0, Minecraft.getMinecraft().thePlayer);
						Minecraft.getMinecraft().playerController.windowClick(
								container.windowId, -999, 0, 5,
								Minecraft.getMinecraft().thePlayer);
						for (int j = 0; j < required.size(); j++) {
							int s = (j - i + required.size()) % required.size();
							Minecraft.getMinecraft().playerController
									.windowClick(container.windowId,
											required.get(s).slotNumber, 1, 5,
											Minecraft.getMinecraft().thePlayer);
						}
						Minecraft.getMinecraft().playerController.windowClick(
								container.windowId, -999, 2, 5,
								Minecraft.getMinecraft().thePlayer);
						Minecraft.getMinecraft().playerController.windowClick(
								container.windowId, present.get(i).slotNumber,
								0, 0, Minecraft.getMinecraft().thePlayer);
					}
				} else {
					for (int i = 0; i < required.size(); i++){
						Minecraft.getMinecraft().playerController.windowClick(
								container.windowId, present.get(i).slotNumber,
								0, 0, Minecraft.getMinecraft().thePlayer);
						Minecraft.getMinecraft().playerController.windowClick(
								container.windowId, required.get(i).slotNumber, 0, 0,
								Minecraft.getMinecraft().thePlayer);
					}
				}
			}
			stage = 1;
			sleeptime = 1;
			return true;
		}
		return false;
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) throws IOException {
		previouslyDown.put(Keyboard.KEY_N, false);
		previouslyDown.put(Keyboard.KEY_M, false);
		previouslyDown.put(Keyboard.KEY_C, false);
		previouslyDown.put(Keyboard.KEY_B, false);
		FMLCommonHandler.instance().bus().register(this);
		loadRecipesFile();
		loadCommandsFile();
	}

}
