package thebombzen.mods.generictweaks.see;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import thebombzen.mods.generictweaks.ReflectionHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;

@Mod(modid = "generictweaks.see", name = "GT See", version = "1.0")
public class See {

	public static boolean enabled = false;

	public int framebufferID = 0;
	public int colorTextureID = 0;
	public int depthRenderBufferID = 0;

	public int dw;
	public int dh;
	public int scaleFactor;
	
	public static final Minecraft mc = Minecraft.getMinecraft();

	@SubscribeEvent
	public void keyInput(KeyInputEvent event) {
		if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD6)
				&& !Keyboard.isRepeatEvent()) {
			enabled = !enabled;
			if (enabled) {
				mc.thePlayer
						.addChatMessage(new ChatComponentText("GT See enabled."));
			} else {
				mc.thePlayer
						.addChatMessage(new ChatComponentText("GT See disabled."));
			}
		}
	}

	// public static Block chest = new
	// BlockSeeChest(0).setHardness(2.5F).setStepSound(Block.soundTypeWood).setBlockName("chest");
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		// System.out.println(Arrays.toString(FMLControlledNamespacedRegistry.class.getDeclaredMethods()));
	}

	@SubscribeEvent
	public void render(RenderWorldLastEvent event) {

		if (!enabled || mc.theWorld == null) {
			return;
		}

		ScaledResolution res = new ScaledResolution(mc,
				mc.displayWidth,
				mc.displayHeight);

		if (dw != res.getScaledWidth() || dh != res.getScaledHeight()) {
			resetFB(res);
		}

		glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		for (Object o : event.context.tileEntities) {
			TileEntity e = (TileEntity) o;
			if (e.getBlockType().equals(Blocks.chest) || e.getBlockType().equals(Blocks.trapped_chest)) {
				TileEntityRendererDispatcher.instance.renderTileEntity(e,
						event.partialTicks);
			}
		}

		for (Object o : mc.theWorld.loadedEntityList) {
			Entity e = (Entity) o;
			if (e instanceof EntityPlayer
					&& e != mc.thePlayer) {
				RenderManager.instance
						.renderEntitySimple(e, event.partialTicks);
			}
		}
		
		if (!ForgeHooksClient.renderFirstPersonHand(event.context, event.partialTicks, 0))
        {
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            ReflectionHelper.invokePrivateMethod(mc.entityRenderer, EntityRenderer.class, new String[]{"renderHand", "func_78476_b", "b"}, new Class<?>[]{float.class, int.class}, event.partialTicks, 0);
        }

		glBindFramebuffer(GL_FRAMEBUFFER, 0);

	}

	@SubscribeEvent
	public void renderGameOverlay(RenderGameOverlayEvent.Pre event) {
		if (enabled && ElementType.ALL.equals(event.type)) {
			mc.entityRenderer.setupOverlayRendering();
			glEnable(GL11.GL_BLEND);
			glDisable(GL11.GL_DEPTH_TEST);
			glDepthMask(false);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			glBindTexture(GL_TEXTURE_2D, colorTextureID);
			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(0.0D, dh / scaleFactor, -90.0D, 0.0D,
					0.0D);
			tessellator.addVertexWithUV(dw / scaleFactor, dh / scaleFactor,
					-90.0D, 1, 0D);
			tessellator.addVertexWithUV(dw / scaleFactor, 0.0D, -90.0D, 1, 1);
			tessellator.addVertexWithUV(0.0D, 0.0D, -90.0D, 0.0D, 1);
			tessellator.draw();
			glDepthMask(true);
			glEnable(GL11.GL_DEPTH_TEST);
			glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}

	}

	public void resetFB(ScaledResolution res) {
		if (framebufferID != 0) {
			glDeleteFramebuffers(framebufferID);
		}
		if (colorTextureID != 0) {
			glDeleteTextures(colorTextureID);
		}
		if (depthRenderBufferID != 0) {
			glDeleteRenderbuffers(depthRenderBufferID);
		}
		scaleFactor = res.getScaleFactor();
		dw = scaleFactor * res.getScaledWidth();
		dh = scaleFactor * res.getScaledHeight();
		framebufferID = glGenFramebuffers(); // create a new framebuffer
		colorTextureID = glGenTextures(); // and a new ture used as a color
									// buffer
		depthRenderBufferID = glGenRenderbuffers(); // And finally a new
													// depthbuffer
		glBindFramebuffer(GL_FRAMEBUFFER, framebufferID); // switch to
															// the new
															// framebuffer
		// initialize color ture
		glBindTexture(GL_TEXTURE_2D, colorTextureID); // Bind the colorbuffer
												// ture
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR); // make
																	// it
																	// linear
																	// filterd
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, dw, dh, 0, GL_RGBA, GL_INT,
				(java.nio.ByteBuffer) null); // Create the ture data
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,
				colorTextureID, 0); // attach it to the
									// framebuffer

		// initialize depth renderbuffer
		glBindRenderbuffer(GL_RENDERBUFFER, depthRenderBufferID); // bind
																	// the
																	// depth
																	// renderbuffer
		glRenderbufferStorage(GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, dw,
				dh); // get the data space for it
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
				GL_RENDERBUFFER, depthRenderBufferID); // bind it to the
														// renderbuffer
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
}
