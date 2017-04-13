package com.helospark.mycraft.mycraft.services;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.actor.GameItem;
import com.helospark.mycraft.mycraft.attributes.Model;
import com.helospark.mycraft.mycraft.helpers.ArrayHelper;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.render.MyTexture;
import com.helospark.mycraft.mycraft.render.MyTextureLoader;
import com.helospark.mycraft.mycraft.shader.Shader;
import com.helospark.mycraft.mycraft.singleton.Singleton;

@Service
public class SpriteWriterService {
	public static final String SPRITE_NAME = "SPRITE_NAME";
	public static final Vector3f DEFAULT_COLOR = new Vector3f(1.0f, 1.0f, 1.0f);
	public static final int BOLD = Font.BOLD;
	public static final int ITALIC = Font.ITALIC;
	public static final int UNDERLINED = 4;
	private static final int MAX_TEXT_CHARACTERS = 1000;
	public Map<String, TextTexture> textTextures = new HashMap<String, TextTexture>();
	MyTextureLoader textureHandler;
	private static final int END = 126;
	private static final int START = 33;
	private Model quad;
	private int vao = -1;

	// cache
	IntVector uvVector = new IntVector();
	float[] textData;
	int[] textIndices;

	float[] data;
	int[] indices;
	Model cacheModel = new Model();

	public SpriteWriterService() {
		ApplicationContext context = Singleton.getInstance().getContext();
		textureHandler = context.getBean(MyTextureLoader.class);
	}

	public void registerSprite(MyTexture texture, int itemWidth, int itemHeight) {
		textTextures.put(SPRITE_NAME, new TextTexture(texture, itemWidth,
				itemHeight));
	}

	public void createText(String font, int resolution) {
		this.createText(font, resolution, 0);
	}

	public void createText(String font, int resolution, int type) {
		int count = END - START + 1;

		int columns = ((int) Math.sqrt(count)) + 1;
		int nearestPowerOfTwo = getNearestPowerOfTwo(resolution, columns);
		resolution = getSizeToBeClosestToPowerOfTwo(resolution, columns,
				nearestPowerOfTwo);
		BufferedImage image = createBufferedImage(font, resolution, columns,
				nearestPowerOfTwo, type);

		MyTexture openglTexture = createOpenglTexture(image);

		TextTexture textTexture = new TextTexture(openglTexture, resolution,
				resolution);
		textTexture.setCharacterBounds(START, END);

		writeTestImage(image);
		textTextures.put(font, textTexture);
	}

	private MyTexture createOpenglTexture(BufferedImage image) {
		MyTexture texture = textureHandler
				.createTextureFromBufferedImage(image);
		return texture;
	}

	private void writeTestImage(BufferedImage image) {
		try {
			ImageIO.write(image, "png", new File("font.png"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private int getSizeToBeClosestToPowerOfTwo(int resolution, int columns,
			int nearestPowerOfTwo) {
		int i = 0;
		while ((resolution + i) * columns < nearestPowerOfTwo) {
			++i;
		}

		--i;
		resolution += i;
		return resolution;
	}

	private BufferedImage createBufferedImage(String font, int resolution,
			int columns, int nearestPowerOfTwo, int type) {
		BufferedImage image = new BufferedImage(nearestPowerOfTwo,
				nearestPowerOfTwo, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graph = (Graphics2D) image.getGraphics();
		graph.setColor(new Color(0, 0, 0, 0));
		graph.fillRect(0, 0, image.getWidth(), image.getHeight());

		graph.setFont(new Font(font, type, resolution));

		int row = 0;
		int coloumn = 0;
		graph.setColor(new Color(255, 255, 255, 255));
		graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		FontMetrics fm = graph.getFontMetrics();
		for (int c = START; c <= END; c++) {
			String text = ((char) c) + "";
			LineMetrics lineMetrics = fm.getLineMetrics(text, graph);
			int height = (int) lineMetrics.getHeight();
			int width = fm.stringWidth(text);
			int x = (resolution - width) / 2;
			graph.drawString(text, coloumn * resolution + x, (row + 1)
					* resolution - lineMetrics.getDescent());
			++coloumn;
			if (coloumn >= columns) {
				++row;
				coloumn = 0;
			}
		}
		return image;
	}

	private int getNearestPowerOfTwo(int resolution, int columns) {
		int nearestPowerOfTwo = 1;

		for (nearestPowerOfTwo = 1; nearestPowerOfTwo <= 4096; nearestPowerOfTwo *= 2) {
			if (nearestPowerOfTwo >= columns * resolution) {
				break;
			}
		}
		return nearestPowerOfTwo;
	}

	public void fillBatchFromText(String text, Vector3f position,
			Vector3f color, float scale, List<Float> dataList,
			List<Integer> indexList, Shader shader, String font) {
		fillUpVao(shader);
		TextTexture texture = textTextures.get(font);
		if (texture == null) {
			texture = textTextures.values().iterator().next();
		}

		if (textData == null) {
			textData = new float[MAX_TEXT_CHARACTERS * quad.getVertexCount()
					* quad.getVertexSize()];
		}
		if (textIndices == null) {
			textIndices = new int[MAX_TEXT_CHARACTERS * quad.getIndexCount()];
		}

		Vector3f charPosition = new Vector3f(position);
		int indicesIndex = 0;
		int verticesIndex = 0;
		int index = dataList.size()
				/ (quad.getVertexCount() * quad.getVertexSize());
		float dx = texture.getDx();
		float dy = texture.getDy();
		for (int i = 0; i < text.length(); ++i) {
			if (texture.contains(text.charAt(i))) {
				cacheModel.copyFrom(quad);
				// Model model = new Model(quad); // HUGE memory here
				cacheModel.applyTranslation(charPosition, scale);
				cacheModel.applyColor(color);
				float u = texture.getU(text.charAt(i));
				float v = texture.getV(text.charAt(i));
				cacheModel.applyUv(0, u + dx, v + dy);
				cacheModel.applyUv(1, u, v + dy);
				cacheModel.applyUv(2, u, v);
				cacheModel.applyUv(3, u + dx, v);

				verticesIndex += cacheModel.addVertexDataToArray(textData,
						verticesIndex);
				indicesIndex += cacheModel.addIndexDataToArray(textIndices,
						indicesIndex, index * cacheModel.getVertexCount());
				++index;
			}
			charPosition.x += scale;
		}
		ArrayHelper.copyToList(textData, dataList);
		ArrayHelper.copyToList(textIndices, indexList);
	}

	public void createBatchFromGameItem(GameItem item, Vector3f position,
			Vector3f scale, List<Float> dataList, Shader shader,
			List<Integer> indexList) {
		int u = (int) item.getU();
		int v = (int) item.getV();
		uvVector.set(u, v, 0);
		createBatchFromTexturePosition(uvVector, position, scale, dataList,
				shader, indexList);
	}

	private void fillUpVao(Shader shader) {
		if (vao == -1) {
			vao = quad.getVaoForProgram(shader);
		}
	}

	public int getTextureForFont(String font) {
		TextTexture texture = textTextures.get(font);
		return texture.getTextureId();
	}

	public void setUnitQuad(Model unitQuad) {
		this.quad = unitQuad;
	}

	public int getTextureForSprite() {
		TextTexture texture = textTextures.get(SPRITE_NAME);
		return texture.getTextureId();
	}

	public void createBatchFromTexturePosition(IntVector uv, Vector3f position,
			Vector3f scale, List<Float> dataList, Shader shader,
			List<Integer> indexList) {
		fillUpVao(shader);
		TextTexture texture = textTextures.get(SPRITE_NAME);

		if (data == null) {
			data = new float[quad.getVertexCount() * quad.getVertexSize()];
		}
		if (indices == null) {
			indices = new int[quad.getIndexCount()];
		}

		int indicesIndex = 0;
		int verticesIndex = 0;
		int index = dataList.size()
				/ (quad.getVertexCount() * quad.getVertexSize());
		float dx = texture.getDx();
		float dy = texture.getDy();

		cacheModel.copyFrom(quad);
		// Model model = new Model(quad); // TODO: optimize
		cacheModel.applyTranslation(position, scale);
		cacheModel.applyColor(DEFAULT_COLOR);

		float u = uv.x * dx;
		float v = uv.y * dy;

		cacheModel.applyUv(0, u + dx, v + dy);
		cacheModel.applyUv(1, u, v + dy);
		cacheModel.applyUv(2, u, v);
		cacheModel.applyUv(3, u + dx, v);
		verticesIndex += cacheModel.addVertexDataToArray(data, verticesIndex);
		indicesIndex += cacheModel.addIndexDataToArray(indices, indicesIndex,
				index * cacheModel.getVertexCount());
		ArrayHelper.copyToList(data, dataList);
		ArrayHelper.copyToList(indices, indexList);
	}
}