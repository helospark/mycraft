package com.helospark.mycraft.mycraft.game;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.blockupdaters.BlockUpdateManager;
import com.helospark.mycraft.mycraft.mathutils.BoundingBox;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.messages.BlockDestroyedMessage;
import com.helospark.mycraft.mycraft.messages.GenericIntegerVectorMessage;
import com.helospark.mycraft.mycraft.messages.NewBlockMessage;
import com.helospark.mycraft.mycraft.services.ChunkFileHandlerService;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

@Service
public class GameMap implements MessageListener {
	private Map<String, Chunk> generatedChunks = new HashMap<String, Chunk>();
	private MessageHandler messager;
	@Autowired
	private ChunkFileHandlerService chunkFileHandlerService;
	@Autowired
	private BlockUpdateManager blockUpdateManager;

	public GameMap() {
		ApplicationContext context = Singleton.getInstance().getContext();
		messager = context.getBean(MessageHandler.class);
		messager.registerListener(this, MessageTypes.BLOCK_DESTROYED_MESSAGE);
		messager.registerListener(this, MessageTypes.NEW_BLOCK_MESSAGE);
		messager.registerListener(this, MessageTypes.LOAD_CHUNK_MESSAGE);
		messager.registerListener(this, MessageTypes.UNLOAD_CHUNK_MESSAGE);
	}

	// private boolean containsChunk(Camera activeCamera, Chunk chunk) {
	// // return activeCamera.getFrustum().containsBox(chunk.getBoundingBox())
	// // != CullingResult.FULLY_OUT;
	// return true;
	// }
	//
	// private boolean isCloseEnough(Camera activeCamera, Chunk chunk) {
	// // Vector3f distance = new Vector3f();
	// // Vector3f.sub(activeCamera.getPosition(), chunk.getPosition(),
	// // distance);
	// // float distanceScalar = distance.length();
	// // return distanceScalar < MAX_RENDER_DISTANCE;
	// return true;
	// }

	public boolean isChunkGenerated(int x, int y, int z) {
		return generatedChunks.get(getKeyFromCoordinate(x, y, z)) != null;
	}

	public void setChunk(int x, int y, int z, Chunk newChunk) {
		generatedChunks.put(getKeyFromCoordinate(x, y, z), newChunk);
	}

	public Chunk getChunk(int x, int y, int z) {
		return generatedChunks.get(getKeyFromCoordinate(x, y, z));
	}

	private String getKeyFromCoordinate(int x, int y, int z) {
		return x + " " + y + " " + z;
	}

	public Chunk getChunkForPosition(int neighbourBlockX, int neighbourBlockY, int neighbourBlockZ) {

		if (neighbourBlockX % Chunk.CHUNK_BLOCK_SIZE == 0) {
			if (neighbourBlockX > 0) {
				--neighbourBlockX;
			} else if (neighbourBlockX < 0) {
				++neighbourBlockX;
			}
		}
		if (neighbourBlockY % Chunk.CHUNK_BLOCK_SIZE == 0) {
			if (neighbourBlockY > 0) {
				--neighbourBlockY;
			} else if (neighbourBlockY < 0) {
				++neighbourBlockY;
			}
		}
		if (neighbourBlockZ % Chunk.CHUNK_BLOCK_SIZE == 0) {
			if (neighbourBlockZ > 0) {
				--neighbourBlockZ;
			} else if (neighbourBlockZ < 0) {
				++neighbourBlockZ;
			}
		}

		int chunkPositionX = (int) Math.floor(((float) neighbourBlockX) / Chunk.CHUNK_UNIT_SIZE);
		int chunkPositionY = (int) Math.floor(((float) neighbourBlockY) / Chunk.CHUNK_UNIT_SIZE);
		int chunkPositionZ = (int) Math.floor(((float) neighbourBlockZ) / Chunk.CHUNK_UNIT_SIZE);

		return generatedChunks.get(getKeyFromCoordinate(chunkPositionX, chunkPositionY,
				chunkPositionZ));
	}

	public static IntVector getIntPositionFromPosition(Vector3f tmp) {
		float x = tmp.x;
		float y = tmp.y;
		float z = tmp.z;

		return new IntVector((int) Math.floor(x / Block.SIZE), (int) Math.floor(y / Block.SIZE),
				(int) Math.floor(z / Block.SIZE));
	}

	@Override
	public boolean receiveMessage(Message message) {
		if (message.getType() == MessageTypes.BLOCK_DESTROYED_MESSAGE) {
			handleBlockDestruction(message);
		} else if (message.getType() == MessageTypes.NEW_BLOCK_MESSAGE) {
			handleBlockPlacement(message);
		} else if (message.getType() == MessageTypes.LOAD_CHUNK_MESSAGE) {
			GenericIntegerVectorMessage intVectorMessage = (GenericIntegerVectorMessage) message;
			loadChunk(intVectorMessage.getX(), intVectorMessage.getY(), intVectorMessage.getZ());
		} else if (message.getType() == MessageTypes.UNLOAD_CHUNK_MESSAGE) {
			GenericIntegerVectorMessage intVectorMessage = (GenericIntegerVectorMessage) message;
			unloadChunk(intVectorMessage.getX(), intVectorMessage.getY(), intVectorMessage.getZ());
		}
		return false;
	}

	public void handleBlockPlacement(Message message) {
		NewBlockMessage blockMessage = (NewBlockMessage) message;
		IntVector position = blockMessage.getPosition();
		int id = blockMessage.getBlockId();

		Chunk chunk = getChunkForPosition(position.x, position.y, position.z);
		IntVector blockPosition = chunk.getIntPositionInRange(position);

		chunk.setBlockAt(blockPosition, id);
		Block block = Blocks.getBlockForId(id);
		if (block.getBlockTimeUpdater() != null) {
			blockUpdateManager.registerTimeUpdatedBlock(blockPosition, block, block
					.getBlockTimeUpdater().getDefaultTime());
		}

		// TODO: do something here
		new RenderableBlock(id);
		messager.sendMessage(new GenericIntegerVectorMessage(
				MessageTypes.BLOCK_DESTRUCTION_HANDLED, Message.MESSAGE_TARGET_ANYONE,
				blockPosition.x, blockPosition.y, blockPosition.z));
	}

	private void handleBlockDestruction(Message message) {
		BlockDestroyedMessage blockDestroyedMessage = (BlockDestroyedMessage) message;

		int x = blockDestroyedMessage.getX();
		int y = blockDestroyedMessage.getY();
		int z = blockDestroyedMessage.getZ();
		IntVector position = new IntVector(x, y, z);
		Block block = getBlockAtPosition(position);
		if (block == null) {
			return;
		}
		BoundingBox boundingBox = blockDestroyedMessage.getBoundingBox();
		if (boundingBox == null) {
			boundingBox = block.getBoundingBox().getBoundingBoxWithPosition(x * Block.SIZE,
					y * Block.SIZE, z * Block.SIZE);
		}

		removeBlockAtPositionWithMessage(position, boundingBox);

		// int blockId = blockDestroyedMessage.getBlockId();
		// Block block = Blocks.getBlockForId(blockId);
		// if (block != null && blockDestroyedMessage.willDrop()) {
		// dynamicObjectRenderer.dropItems(block, new Vector3f((position.x +
		// 0.5f) * Block.SIZE,
		// (position.y + 0.5f) * Block.SIZE, (position.z + 0.5f) * Block.SIZE));
		// }

	}

	private void removeBlockAtPositionWithMessage(IntVector position, BoundingBox boundingBox) {
		Chunk chunk = getChunkForPosition(position.x, position.y, position.z);
		IntVector blockPosition = chunk.getIntPositionInRange(position);

		chunk.setBlockAt(blockPosition, Blocks.get("Air"));
		messager.sendImmediateMessage(new BlockDestroyedMessage(
				MessageTypes.BLOCK_DESTROYED_HANDLED, Message.MESSAGE_TARGET_ANYONE, position.x,
				position.y, position.z, boundingBox, false));
	}

	public void loadChunk(int x, int y, int z) {
		String key = getKeyFromCoordinate(x, y, z);
		if (generatedChunks.get(key) != null) {
			// generatedChunks.remove(key);
			// unloadChunk(x, y, z);
		} else {
			Chunk chunk = chunkFileHandlerService.readChunk(x, y, z);
			if (chunk != null) {
				generatedChunks.put(key, chunk);
				messager.sendImmediateMessage(new GenericIntegerVectorMessage(
						MessageTypes.CHUNK_GENERATED_MESSAGE, Message.MESSAGE_TARGET_ANYONE, x, y,
						z));
			}
		}
	}

	public void unloadChunk(int x, int y, int z) {
		String key = getKeyFromCoordinate(x, y, z);
		Chunk chunk = generatedChunks.get(key);
		if (chunk != null) {
			generatedChunks.remove(key);
			chunkFileHandlerService.writeChunk(chunk);
			chunk.clearAllLoadedData();
			chunk = null;
		}
	}

	public Map<String, Chunk> getLoadedChunks() {
		return generatedChunks;
	}

	public Block getBlockAtPosition(IntVector newPosition) {
		Chunk chunk = getChunkForPosition(newPosition.x, newPosition.y, newPosition.z);
		if (chunk == null) {
			return null;
		}
		IntVector blockPosition = chunk.getIntPositionInRange(newPosition);
		return chunk.getBlock(blockPosition.x, blockPosition.y, blockPosition.z);
	}

	public BlockSide[] getVisibleBlockSides(IntVector blockPosition) {
		Chunk chunk = getChunkForPosition(blockPosition.x, blockPosition.y, blockPosition.z);
		IntVector chunkBlockPosition = chunk.getIntPositionInRange(blockPosition);
		Block block = chunk.getBlock(chunkBlockPosition.x, chunkBlockPosition.y,
				chunkBlockPosition.z);
		BoundingBox boundingBox = block.getBoundingBox().getBoundingBoxWithPosition(
				blockPosition.x * Block.SIZE, blockPosition.y * Block.SIZE,
				blockPosition.z * Block.SIZE);
		return chunk.getVisibleBlockSides(blockPosition, boundingBox);
	}

	public static void fillVector3fFromIntvector(Vector3f vector, IntVector intVector) {
		vector.x = intVector.x * Block.SIZE;
		vector.y = intVector.y * Block.SIZE;
		vector.z = intVector.z * Block.SIZE;
	}
}
