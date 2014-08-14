package thebombzen.mods.generictweaks.open;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;

@Mod(modid = "generictweaks.open", name = "GT Open", version = "1.0")
public class Open {

	public static boolean enabled = false;
	
	public static final Minecraft mc = Minecraft.getMinecraft();

	public MovingObjectPosition chestRayTrace(float partialTickTime) {
		float distance = mc.playerController.getBlockReachDistance();
		Vec3 vec3 = mc.thePlayer.getPosition(partialTickTime);
		Vec3 vec31 = mc.thePlayer.getLook(partialTickTime);
		Vec3 vec32 = vec3.addVector(vec31.xCoord * distance, vec31.yCoord
				* distance, vec31.zCoord * distance);
		return reallyRayTrace(vec3, vec32);
	}

	@SubscribeEvent
	public void mouseInput(MouseEvent event) {
		if (!enabled || mc.theWorld == null || mc.currentScreen != null) {
			return;
		}
		if (Mouse.getEventButton() != 1) {
			return;
		}
		MovingObjectPosition position = chestRayTrace(0);
		if (position != null) {
			Minecraft.getMinecraft().objectMouseOver = position;
		}
		// if (position != null){
		// mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld,
		// mc.thePlayer.inventory.getCurrentItem(), position.blockX,
		// position.blockY, position.blockZ, position.sideHit, position.hitVec);
		// }
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}

	public MovingObjectPosition reallyRayTrace(Vec3 position, Vec3 finalPosition) {
		if (!Double.isNaN(position.xCoord) && !Double.isNaN(position.yCoord)
				&& !Double.isNaN(position.zCoord)) {
			if (!Double.isNaN(finalPosition.xCoord)
					&& !Double.isNaN(finalPosition.yCoord)
					&& !Double.isNaN(finalPosition.zCoord)) {
				int i = MathHelper.floor_double(finalPosition.xCoord);
				int j = MathHelper.floor_double(finalPosition.yCoord);
				int k = MathHelper.floor_double(finalPosition.zCoord);
				int l = MathHelper.floor_double(position.xCoord);
				int i1 = MathHelper.floor_double(position.yCoord);
				int j1 = MathHelper.floor_double(position.zCoord);
				Block block = mc.theWorld.getBlock(l, i1, j1);
				int k1 = mc.theWorld.getBlockMetadata(l, i1, j1);

				if ((Blocks.chest.equals(block) || Blocks.trapped_chest.equals(block))
						&& block.canCollideCheck(k1, false)) {
					MovingObjectPosition movingobjectposition = block
							.collisionRayTrace(mc.theWorld, l, i1, j1,
									position, finalPosition);

					if (movingobjectposition != null) {
						return movingobjectposition;
					}
				}

				MovingObjectPosition movingobjectposition2 = null;
				k1 = 200;

				while (k1-- >= 0) {
					if (Double.isNaN(position.xCoord)
							|| Double.isNaN(position.yCoord)
							|| Double.isNaN(position.zCoord)) {
						return null;
					}

					if (l == i && i1 == j && j1 == k) {
						return movingobjectposition2;
					}

					boolean flag6 = true;
					boolean flag3 = true;
					boolean flag4 = true;
					double d0 = 999.0D;
					double d1 = 999.0D;
					double d2 = 999.0D;

					if (i > l) {
						d0 = l + 1.0D;
					} else if (i < l) {
						d0 = l + 0.0D;
					} else {
						flag6 = false;
					}

					if (j > i1) {
						d1 = i1 + 1.0D;
					} else if (j < i1) {
						d1 = i1 + 0.0D;
					} else {
						flag3 = false;
					}

					if (k > j1) {
						d2 = j1 + 1.0D;
					} else if (k < j1) {
						d2 = j1 + 0.0D;
					} else {
						flag4 = false;
					}

					double d3 = 999.0D;
					double d4 = 999.0D;
					double d5 = 999.0D;
					double d6 = finalPosition.xCoord - position.xCoord;
					double d7 = finalPosition.yCoord - position.yCoord;
					double d8 = finalPosition.zCoord - position.zCoord;

					if (flag6) {
						d3 = (d0 - position.xCoord) / d6;
					}

					if (flag3) {
						d4 = (d1 - position.yCoord) / d7;
					}

					if (flag4) {
						d5 = (d2 - position.zCoord) / d8;
					}

					byte b0;

					if (d3 < d4 && d3 < d5) {
						if (i > l) {
							b0 = 4;
						} else {
							b0 = 5;
						}

						position.xCoord = d0;
						position.yCoord += d7 * d3;
						position.zCoord += d8 * d3;
					} else if (d4 < d5) {
						if (j > i1) {
							b0 = 0;
						} else {
							b0 = 1;
						}

						position.xCoord += d6 * d4;
						position.yCoord = d1;
						position.zCoord += d8 * d4;
					} else {
						if (k > j1) {
							b0 = 2;
						} else {
							b0 = 3;
						}

						position.xCoord += d6 * d5;
						position.yCoord += d7 * d5;
						position.zCoord = d2;
					}

					Vec3 vec32 = Vec3.createVectorHelper(position.xCoord,
							position.yCoord, position.zCoord);
					l = (int) (vec32.xCoord = MathHelper
							.floor_double(position.xCoord));

					if (b0 == 5) {
						--l;
						++vec32.xCoord;
					}
					
					i1 = (int) (vec32.yCoord = MathHelper
							.floor_double(position.yCoord));

					if (b0 == 1) {
						--i1;
						++vec32.yCoord;
					}

					j1 = (int) (vec32.zCoord = MathHelper
							.floor_double(position.zCoord));

					if (b0 == 3) {
						--j1;
						++vec32.zCoord;
					}

					Block block1 = mc.theWorld.getBlock(l, i1, j1);
					int l1 = mc.theWorld.getBlockMetadata(l, i1, j1);
					if (Blocks.chest.equals(block1) || Blocks.trapped_chest.equals(block1)) {
						if (block1.canCollideCheck(l1, false)) {
							MovingObjectPosition movingobjectposition1 = block1
									.collisionRayTrace(mc.theWorld, l, i1, j1,
											position, finalPosition);
							if (movingobjectposition1 != null) {
								return movingobjectposition1;
							}
						} else {
							movingobjectposition2 = new MovingObjectPosition(l,
									i1, j1, b0, position, false);
						}
					} else {
						movingobjectposition2 = null;
					}
				}

				return movingobjectposition2;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	@SubscribeEvent
	public void keyInput(KeyInputEvent event){
		if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD4) && !Keyboard.isRepeatEvent()){
			enabled = !enabled;
			if (enabled){
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("GT Open enabled."));
			} else {
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("GT Open disabled."));
			}
		}
	}

}
