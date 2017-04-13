package com.helospark.mycraft.mycraft.render;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.boundaries.DefaultJavaFileHandler;
import com.helospark.mycraft.mycraft.helpers.XmlHelpers;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.xml.XmlLoader;

@Service
public class MyTextureLoader implements XmlLoader {
	private Map<String, MyTexture> textures = new HashMap<String, MyTexture>();
	private DefaultJavaFileHandler fileHandler;

	public MyTextureLoader() {
		ApplicationContext context = Singleton.getInstance().getContext();
		fileHandler = context.getBean(DefaultJavaFileHandler.class);
	}

	public MyTexture load2DTexture(String fileName) {
		MyTexture texture = textures.get(fileName);
		if (texture != null) {
			return texture;
		}
		MyTexture loadedTexture = loadTextureFromFile(fileName);
		textures.put(fileName, loadedTexture);
		initializeBlockImage(loadedTexture);
		return loadedTexture;
	}

	private void initializeBlockImage(MyTexture loadedTexture) {
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, loadedTexture.getTextureId());
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_NEAREST_MIPMAP_LINEAR);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	private void initializeRegularTexture(MyTexture loadedTexture) {
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, loadedTexture.getTextureId());
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_LINEAR_MIPMAP_LINEAR);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public MyTexture loadTextureFromFile(String fileName) {
		String[] splittedString = fileName.split("\\.");

		String extension = null;
		if (splittedString.length == 0) {
			extension = "PNG";
		} else {
			extension = splittedString[splittedString.length - 1].toUpperCase();
		}
		Texture texture;
		try {
			texture = TextureLoader.getTexture(extension, fileHandler.toStream(fileName));
		} catch (IOException e) {
			throw new RuntimeException("Unable to load texture from stream");
		}
		return new MyTexture(GL11.GL_TEXTURE_2D, texture.getTextureID(), texture.getImageWidth(),
				texture.getImageHeight());
	}

	public MyTexture createTextureFromBufferedImage(BufferedImage image) {
		Texture texture;
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(image, "png", os);
			InputStream is = new ByteArrayInputStream(os.toByteArray());
			texture = TextureLoader.getTexture("png", is);
		} catch (Exception ex) {
			throw new RuntimeException("Unable to create a texture from a bufferedImage " + ex);
		}
		MyTexture result = new MyTexture(GL11.GL_TEXTURE_2D, texture.getTextureID(),
				image.getWidth(), image.getHeight());
		initializeRegularTexture(result);
		return result;
	}

	public MyTexture getTextureFromName(String textureName) {
		return textures.get(textureName);
	}

	@Override
	public void parseXml(Element rootElement) {
		String textureName = rootElement.getAttribute("name");
		String textureSource = XmlHelpers.getStringValue(rootElement, "source");
		MyTexture loadedTexture = loadTextureFromFile(textureSource);
		initializeBlockImage(loadedTexture);
		textures.put(textureName, loadedTexture);
	}
}
