package com.helospark.mycraft.mycraft.window;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.messages.GenericIntMessage;
import com.helospark.mycraft.mycraft.services.GlobalParameters;
import com.helospark.mycraft.mycraft.singleton.Singleton;

@Service
public class Window implements MessageListener {
	private static final int BITS_PER_PIXEL = 24;
	private static final int CUSTOM_CURSOR_SIZE = 5;
	private int width, height;
	private boolean isFullScreen = false;
	Cursor[] cursors = new Cursor[2];
	private GlobalParameters globalParameters;

	public Window() {
		ApplicationContext context = Singleton.getInstance().getContext();
		MessageHandler messager = context.getBean(MessageHandler.class);
		messager.registerListener(this, MessageTypes.CURSOR_SHOW_STATUS);
		messager.registerListener(this, MessageTypes.TURN_OFF_CURSOR);
		messager.registerListener(this, MessageTypes.TURN_ON_CURSOR);
		messager.registerListener(this, MessageTypes.ESCAPE_REQUESTED);
		globalParameters = context.getBean(GlobalParameters.class);
	}

	public Window(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void show() {
		width = globalParameters.initialWindowWidth;
		height = globalParameters.initialWindowHeight;
		isFullScreen = globalParameters.isFullScreen;
		try {
			DisplayMode displayMode = null;
			Display.setFullscreen(isFullScreen);
			DisplayMode displayModes[] = Display.getAvailableDisplayModes();
			for (int i = 0; i < displayModes.length; i++) {

				if (displayModes[i].getWidth() == width && displayModes[i].getHeight() == height) {
					displayMode = displayModes[i];
					break;
				}
			}
			Display.setDisplayMode(displayMode);
			Display.setTitle("title");
			Display.create();
			GL11.glClearColor(globalParameters.fogColor.x, globalParameters.fogColor.y,
					globalParameters.fogColor.z, 1.0f);

			createCursors();

		} catch (LWJGLException e) {
			throw new RuntimeException("Unable to create display ", e);
		}
	}

	private void createCursors() {
		try {
			IntBuffer intBuffer = BufferUtils.createIntBuffer(1);
			intBuffer.put(0xff000000);
			intBuffer.flip();
			cursors[0] = new Cursor(1, 1, 0, 0, 1, intBuffer, null);
		} catch (LWJGLException e) {
			// Not to worry, we don't care about cursors that much
		}
		cursors[1] = Mouse.getNativeCursor();
	}

	public boolean isCloseRequested() {
		return Display.isCloseRequested();
	}

	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		try {
			Display.setDisplayMode(new DisplayMode(width, height));
		} catch (LWJGLException e) {
			throw new RuntimeException("Unable to create display");
		}
	}

	public void toggleFullScreen(boolean isFullScreen) {
		this.isFullScreen = isFullScreen;
		try {
			Display.setFullscreen(isFullScreen);
		} catch (LWJGLException e) {
			throw new RuntimeException("Unable to create display");
		}
	}

	public float getAspectRation() {
		return (float) width / height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setTitleBarText(double fps, double deltaMilliTime) {
		Display.setTitle("Mycraft " + String.format("%.2f", fps) + " fps "
				+ String.format("%.2f", deltaMilliTime) + " ms");
	}

	@Override
	public boolean receiveMessage(Message message) {
		if (message.getType() == MessageTypes.ESCAPE_REQUESTED) {
			Display.destroy();
		} else if (message.getType() == MessageTypes.CURSOR_SHOW_STATUS) {
			GenericIntMessage intMessage = (GenericIntMessage) message;
			try {
				Cursor currentCursor = cursors[intMessage.getParameter()];
				Mouse.setNativeCursor(currentCursor);
			} catch (LWJGLException e) {
				throw new RuntimeException("Unable to set cursor");
			}
		} else if (message.getType() == MessageTypes.TURN_ON_CURSOR) {
			Cursor currentCursor = cursors[1];
			try {
				Mouse.setNativeCursor(currentCursor);
			} catch (LWJGLException e) {
				throw new RuntimeException("Unable to set cursor");
			}
		} else if (message.getType() == MessageTypes.TURN_OFF_CURSOR) {
			Cursor currentCursor = cursors[0];
			try {
				Mouse.setNativeCursor(currentCursor);
			} catch (LWJGLException e) {
				throw new RuntimeException("Unable to set cursor");
			}
		}
		return false;
	}
}
