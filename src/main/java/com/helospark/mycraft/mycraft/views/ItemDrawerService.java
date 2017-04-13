package com.helospark.mycraft.mycraft.views;

import java.util.List;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.actor.GameItem;
import com.helospark.mycraft.mycraft.actor.GameItems;
import com.helospark.mycraft.mycraft.actor.InventoryItem;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.render.SpriteAndTextBatchData;
import com.helospark.mycraft.mycraft.services.SpriteWriterService;
import com.helospark.mycraft.mycraft.shader.Shader;

@Service
public class ItemDrawerService {

	public static final int ACTIVE_ELEMENT = 0;
	public static final int NON_ACTIVE_ELEMENT = 1;
	protected static final int SCALE = 40;
	private static final Vector3f SCALE_VECTOR = new Vector3f(SCALE, SCALE, SCALE);
	private static final IntVector ACTIVE_CELL_VECTOR = new IntVector(10, 0, 0);
	private static final IntVector NON_ACTIVE_CELL_VECTOR = new IntVector(11, 0, 0);
	private static final IntVector ARROW_CELL_VECTOR = new IntVector(12, 0, 0);
	private static final int TEXT_SIZE = 15;
	private static final Vector3f TEXT_COLOR = new Vector3f(1.0f, 1.0f, 1.0f);
	private static final Vector3f ARROW_SCALE = new Vector3f(60, 40, 0);

	@Autowired
	GameItems items;

	@Autowired
	SpriteWriterService spriteWriterService;

	// cache
	private Vector3f position = new Vector3f();

	public ItemDrawerService() {

	}

	public void drawBackground(SpriteAndTextBatchData spriteTextBatchData, Vector3f position,
			int activeElement) {
		List<Float> vertices = spriteTextBatchData
				.getVertexListFor(SpriteAndTextBatchData.BATCH_TYPE_SPRITE);
		List<Integer> indices = spriteTextBatchData
				.getIndexListFor(SpriteAndTextBatchData.BATCH_TYPE_SPRITE);
		Shader programId = spriteTextBatchData
				.getShaderForId(SpriteAndTextBatchData.BATCH_TYPE_SPRITE);

		if (activeElement == ACTIVE_ELEMENT) {
			drawActiveItemBackground(vertices, indices, programId, position);
		} else {
			drawNonActiveItemBackground(vertices, indices, programId, position);
		}
	}

	public void drawOneItemAt(SpriteAndTextBatchData spriteTextBatchData, Vector3f position,
			InventoryItem inventoryItem) {
		List<Float> vertices = spriteTextBatchData
				.getVertexListFor(SpriteAndTextBatchData.BATCH_TYPE_SPRITE);
		List<Integer> indices = spriteTextBatchData
				.getIndexListFor(SpriteAndTextBatchData.BATCH_TYPE_SPRITE);
		Shader programId = spriteTextBatchData
				.getShaderForId(SpriteAndTextBatchData.BATCH_TYPE_SPRITE);

		GameItem gameItem = items.getById(inventoryItem.getId());
		spriteWriterService.createBatchFromGameItem(gameItem, position, SCALE_VECTOR, vertices,
				programId, indices);
		if (inventoryItem.getAmount() > 1) {
			writeText(spriteTextBatchData, position, String.valueOf(inventoryItem.getAmount()));
		}
	}

	private void drawNonActiveItemBackground(List<Float> vertices, List<Integer> indices,
			Shader programId, Vector3f position) {
		spriteWriterService.createBatchFromTexturePosition(NON_ACTIVE_CELL_VECTOR, position,
				SCALE_VECTOR, vertices, programId, indices);
	}

	private void drawActiveItemBackground(List<Float> vertices, List<Integer> indices,
			Shader programId, Vector3f position) {
		spriteWriterService.createBatchFromTexturePosition(ACTIVE_CELL_VECTOR, position,
				SCALE_VECTOR, vertices, programId, indices);
	}

	private void writeText(SpriteAndTextBatchData spriteTextBatchData, Vector3f position,
			String text) {
		List<Float> textVertices = spriteTextBatchData
				.getVertexListFor(SpriteAndTextBatchData.BATCH_TYPE_TEXT);
		List<Integer> textIndices = spriteTextBatchData
				.getIndexListFor(SpriteAndTextBatchData.BATCH_TYPE_TEXT);
		Shader textProgramId = spriteTextBatchData
				.getShaderForId(SpriteAndTextBatchData.BATCH_TYPE_TEXT);
		spriteWriterService.fillBatchFromText(text, position, TEXT_COLOR, TEXT_SIZE, textVertices,
				textIndices, textProgramId, "");
	}

	public void drawInventory(SpriteAndTextBatchData spriteTextBatchData, MenuActions menu) {
		position.set(menu.getPosition());

		drawMatrixOfItems(spriteTextBatchData, menu);
	}

	private void drawMatrixOfItems(SpriteAndTextBatchData spriteTextBatchData, MenuActions menu) {
		for (int i = 0; i < menu.getHeight(); ++i) {
			for (int j = 0; j < menu.getWidth(); ++j) {

				if (menu.getActivePosition().x == j && menu.getActivePosition().y == i) {
					drawBackground(spriteTextBatchData, position, ACTIVE_ELEMENT);
				} else {
					drawBackground(spriteTextBatchData, position, NON_ACTIVE_ELEMENT);
				}

				if (!menu.isEmptyAtPosition(i, j)) {
					InventoryItem item = menu.getItemAtPosition(i, j);
					drawOneItemAt(spriteTextBatchData, position, item);
				}

				position.x += MenuActions.SCALE_VECTOR.x;
			}
			position.x = menu.getPosition().x;
			position.y += MenuActions.SCALE_VECTOR.y;
		}
	}

	public void drawHandView(SpriteAndTextBatchData spriteTextBatchData, MenuActions menu) {
		position.set(menu.getPosition());

		drawMatrixOfItems(spriteTextBatchData, menu);
	}

	public void drawCraftingView(SpriteAndTextBatchData spriteTextBatchData, CraftMenuActions menu) {

		position.set(menu.getPosition());
		drawMatrixOfItems(spriteTextBatchData, menu);

		calculateArrowPosition(menu);
		drawArrow(spriteTextBatchData, position);
		position.set(menu.getResultStartPosition());

		drawBackground(spriteTextBatchData, position, menu.isResultActive() ? ACTIVE_ELEMENT
				: NON_ACTIVE_ELEMENT);

		if (menu.getResultInventoryItem() != null) {
			drawOneItemAt(spriteTextBatchData, position, menu.getResultInventoryItem());
		}
	}

	private void calculateArrowPosition(CraftMenuActions menu) {
		position.x = menu.getPosition().x + menu.getWidth() * CraftMenuActions.SCALE_VECTOR.x + 20;
		position.y = menu.getPosition().y + menu.getHeight() * CraftMenuActions.SCALE_VECTOR.y
				/ 2.0f - CraftMenuActions.ARROW_SCALE.y / 2.0f;
	}

	private void drawArrow(SpriteAndTextBatchData spriteTextBatchData, Vector3f position) {
		List<Float> vertices = spriteTextBatchData
				.getVertexListFor(SpriteAndTextBatchData.BATCH_TYPE_SPRITE);
		List<Integer> indices = spriteTextBatchData
				.getIndexListFor(SpriteAndTextBatchData.BATCH_TYPE_SPRITE);
		Shader programId = spriteTextBatchData
				.getShaderForId(SpriteAndTextBatchData.BATCH_TYPE_SPRITE);

		spriteWriterService.createBatchFromTexturePosition(ARROW_CELL_VECTOR, position,
				CraftMenuActions.ARROW_SCALE, vertices, programId, indices);
	}
}
