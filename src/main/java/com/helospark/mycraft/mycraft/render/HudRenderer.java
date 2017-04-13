package com.helospark.mycraft.mycraft.render;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.actor.GameItem;
import com.helospark.mycraft.mycraft.actor.GameItems;
import com.helospark.mycraft.mycraft.attributes.Model;
import com.helospark.mycraft.mycraft.attributes.SimpleModelCreator;
import com.helospark.mycraft.mycraft.services.SpriteWriterService;
import com.helospark.mycraft.mycraft.shader.Shader;
import com.helospark.mycraft.mycraft.shader.ShaderLoader;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.transformation.Transformation;
import com.helospark.mycraft.mycraft.views.ViewStack;
import com.helospark.mycraft.mycraft.window.Window;

@Service
public class HudRenderer {
	SpriteWriterService spriteWriterService;
	BatchModel batchModel;
	SimpleModelCreator modelCreator;
	Shader textRendererShader;
	Shader spriteRendererShader;
	Transformation transformation;
	int batchRenderVao = -1;
	ShaderLoader shaderLoader;
	Window window;
	String fontId = "TimesRoman";
	GameItems gameItems;
	GameItem testItem;
	ViewStack viewStack;
	SpriteAndTextBatchData spriteTextBatchData = new SpriteAndTextBatchData();

	public HudRenderer() {
		ApplicationContext context = Singleton.getInstance().getContext();
		spriteWriterService = context.getBean(SpriteWriterService.class);
		modelCreator = context.getBean(SimpleModelCreator.class);
		transformation = context.getBean(Transformation.class);
		shaderLoader = context.getBean(ShaderLoader.class);
		viewStack = context.getBean(ViewStack.class);
		window = context.getBean(Window.class);
		Model unitQuad = SimpleModelCreator.getUnitQuad();
		spriteWriterService.setUnitQuad(unitQuad);
		batchModel = new BatchModel(1000);
		batchModel.createPositionAttribute();
		batchModel.createColorAttribute();
		batchModel.createUvAttribute();
		gameItems = context.getBean(GameItems.class);
	}

	public void initialize() {
		spriteWriterService.createText(fontId, 100, SpriteWriterService.BOLD);
		textRendererShader = shaderLoader.loadStandardShaderFromFile(
				"src/main/resources/shaders/textVertex.vs",
				"src/main/resources/shaders/textFragment.fs");
		spriteRendererShader = textRendererShader;
		testItem = gameItems.getById(0);
		spriteTextBatchData.setShader(SpriteAndTextBatchData.BATCH_TYPE_SPRITE,
				textRendererShader);
		spriteTextBatchData.setShader(SpriteAndTextBatchData.BATCH_TYPE_TEXT,
				textRendererShader);
		spriteTextBatchData.setTextureId(
				SpriteAndTextBatchData.BATCH_TYPE_TEXT,
				spriteWriterService.getTextureForFont(fontId));
	}

	public void registerSpriteTexture(MyTexture texture, int itemWidth,
			int itemHeight) {
		spriteWriterService.registerSprite(texture, itemWidth, itemHeight);
		spriteTextBatchData.setTextureId(
				SpriteAndTextBatchData.BATCH_TYPE_SPRITE,
				texture.getTextureId());
	}

	public void render() {
		transformation.pushMatrix();
		transformation.setMatrixMode(Transformation.PROJECTION_MATRIX);
		transformation.loadIdentity();
		transformation
				.ortho(0, window.getWidth(), 0, window.getHeight(), -1, 1);
		transformation.setMatrixMode(Transformation.VIEW_MATRIX);
		transformation.loadIdentity();
		transformation.setMatrixMode(Transformation.MODEL_MATRIX);
		transformation.loadIdentity();

		spriteTextBatchData.clear();
		viewStack.fillBuffers(spriteTextBatchData);

		renderSpriteBatch(spriteTextBatchData);

		transformation.popMatrix();
	}

	private void renderSpriteBatch(SpriteAndTextBatchData spriteAndTextBatchData) {

		for (int i = 0; i < SpriteAndTextBatchData.NUMBER_OF_BATCHES; ++i) {
			int programId = spriteAndTextBatchData.getProgramIdFor(i);
			GL20.glUseProgram(programId);
			int textureId = spriteAndTextBatchData.getTextureId(i);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
			renderBatch(spriteAndTextBatchData.getVertexListFor(i),
					spriteAndTextBatchData.getIndexListFor(i),
					spriteAndTextBatchData.getShaderForId(i));

		}
	}

	private void renderBatch(List<Float> vertices, List<Integer> indices,
			Shader shader) {
		transformation.uploadToOpenglShaders(shader);
		if (batchRenderVao == -1) {
			batchRenderVao = batchModel.getVaoForProgram(shader);
		}
		GL30.glBindVertexArray(batchRenderVao);
		float[] vertexData = new float[vertices.size()];
		int[] indexData = new int[indices.size()];

		copyToArray(vertices, vertexData);
		copyToArray(indices, indexData);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDepthFunc(GL11.GL_ALWAYS);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		batchModel.enableVertexAttribArrays();
		batchModel.streamBuffer(shader, vertexData, vertices.size(), indexData,
				indices.size());
		batchModel.disableVertexAttribArrays();
		GL30.glBindVertexArray(0);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthFunc(GL11.GL_LESS);
		vertices.clear();
		indices.clear();
	}

	private void copyToArray(List<Integer> indices, int[] indexData) {
		for (int i = 0; i < indices.size(); ++i) {
			indexData[i] = indices.get(i);
		}
	}

	private void copyToArray(List<Float> vertices, float[] vertexData) {
		for (int i = 0; i < vertices.size(); ++i) {
			vertexData[i] = vertices.get(i);
		}
	}
}
