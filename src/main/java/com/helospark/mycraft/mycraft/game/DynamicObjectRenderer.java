package com.helospark.mycraft.mycraft.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.actor.Actor;
import com.helospark.mycraft.mycraft.actor.AnimatedActorComponent;
import com.helospark.mycraft.mycraft.actor.GameItem;
import com.helospark.mycraft.mycraft.actor.GameItems;
import com.helospark.mycraft.mycraft.actor.HealthComponent;
import com.helospark.mycraft.mycraft.actor.InventoryItem;
import com.helospark.mycraft.mycraft.actor.ModelRenderComponent;
import com.helospark.mycraft.mycraft.actor.MyIdGenerator;
import com.helospark.mycraft.mycraft.actor.PhysicsComponent;
import com.helospark.mycraft.mycraft.actor.PickUpMovingComponent;
import com.helospark.mycraft.mycraft.actor.TransformComponent;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.messages.ActorLifeMessage;
import com.helospark.mycraft.mycraft.messages.BlockDamageChangeMessage;
import com.helospark.mycraft.mycraft.messages.BlockDestroyingMessage;
import com.helospark.mycraft.mycraft.messages.RenderComponentMessage;
import com.helospark.mycraft.mycraft.render.RenderableModelNode;
import com.helospark.mycraft.mycraft.services.ActorSearchService;
import com.helospark.mycraft.mycraft.shader.FloatShaderUniform;
import com.helospark.mycraft.mycraft.shader.IntegerShaderUniform;
import com.helospark.mycraft.mycraft.shader.Vector3fShaderUniform;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.transformation.Transformation;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;
import com.helospark.mycraft.mycraft.xml.ModelLoader;

@Service
public class DynamicObjectRenderer implements MessageListener {
	MessageHandler messager;
	ActorSearchService actorSearchService;
	MyIdGenerator idGenerator;
	private Map<Integer, RenderableModelNode[]> dynamicObjects = new HashMap<Integer, RenderableModelNode[]>();
	GameItems gameItems;
	Random random;
	@Autowired
	Transformation transformation;

	@Autowired
	ModelLoader modelLoader;

	public DynamicObjectRenderer() {
		ApplicationContext context = Singleton.getInstance().getContext();
		messager = context.getBean(MessageHandler.class);
		messager.registerListener(this, MessageTypes.NEW_RENDER_COMPONENT);
		messager.registerListener(this, MessageTypes.DELETED_RENDER_COMPONENT);
		messager.registerListener(this, MessageTypes.BLOCK_DESTROYING_STARTED);
		messager.registerListener(this, MessageTypes.BLOCK_DESTROYING_ENDED);
		messager.registerListener(this, MessageTypes.BLOCK_DAMAGE_CHANGED_MESSAGE);
		actorSearchService = context.getBean(ActorSearchService.class);
		idGenerator = context.getBean(MyIdGenerator.class);
		gameItems = context.getBean(GameItems.class);
		random = new Random();
	}

	@Override
	public boolean receiveMessage(Message message) {
		if (message.getType() == MessageTypes.NEW_RENDER_COMPONENT) {
			handleNewRenderComponent(message);
		} else if (message.getType() == MessageTypes.DELETED_RENDER_COMPONENT) {
			handleDeletedRenderComponent(message);
		} else if (message.getType() == MessageTypes.BLOCK_DESTROYING_ENDED) {
			int key = ((BlockDestroyingMessage) message).getId();
			dynamicObjects.remove(key);
		} else if (message.getType() == MessageTypes.BLOCK_DESTROYING_STARTED) {
			addDestroyingBlockToList(message);
		} else if (message.getType() == MessageTypes.BLOCK_DAMAGE_CHANGED_MESSAGE) {
			changeDamageOnBlock(message);
		}
		return false;
	}

	void render() {
		GL11.glDisable(GL11.GL_CULL_FACE);
		for (Map.Entry<Integer, RenderableModelNode[]> entry : dynamicObjects.entrySet()) {
			RenderableModelNode[] nodes = entry.getValue();
			for (RenderableModelNode node : nodes) {
				Actor actor = actorSearchService.findActorById(entry.getKey());
				boolean isMatrixPushed = false;
				boolean hasDepthDisabled = false;
				if (actor != null) {
					TransformComponent component = (TransformComponent) (actor
							.getComponent(TransformComponent.TRANSFORM_COMPONENT_NAME));
					node.setPosition(component.getPosition());
					node.setRotation(component.getDirection());
					node.setScale(component.getScale());

					if (component.isLocal()) {
						transformation.setMatrixMode(Transformation.VIEW_MATRIX);
						transformation.pushMatrix();
						transformation.loadIdentity();
						isMatrixPushed = true;
						transformation.setMatrixMode(Transformation.MODEL_MATRIX);
					}

					ModelRenderComponent modelRenderComponent = (ModelRenderComponent) (actor
							.getComponent(ModelRenderComponent.MODEL_RENDER_COMPONENT_NAME));

					if (modelRenderComponent == null) {
						modelRenderComponent = ((AnimatedActorComponent) (actor
								.getComponent(AnimatedActorComponent.ANIMATED_ACTOR_COMPONENT)))
								.getModelRenderComponent();
					}

					if (!modelRenderComponent.hasDepthBuffer()) {
						GL11.glDisable(GL11.GL_DEPTH_TEST);
						hasDepthDisabled = true;
					}

					HealthComponent healthComponent = (HealthComponent) (actor
							.getComponent(HealthComponent.HEALTH_COMPONENT_NAME));
					if (healthComponent != null) {
						if (healthComponent.isCurrentlyHurt()) {
							node.setUniform("colorize", new Vector3fShaderUniform(new Vector3f(
									0.8f, 0.0f, 0.0f)));
						} else {
							node.setUniform("colorize", new Vector3fShaderUniform(new Vector3f(
									0.0f, 0.0f, 0.0f)));
						}
					}

				}
				node.preBatchRender();
				node.preRender();
				node.render();
				node.postRender();
				node.postBatchRender();
				if (isMatrixPushed) {
					transformation.setMatrixMode(Transformation.VIEW_MATRIX);
					transformation.popMatrix();
					transformation.setMatrixMode(Transformation.MODEL_MATRIX);
				}
				if (hasDepthDisabled) {
					GL11.glEnable(GL11.GL_DEPTH_TEST);
				}
			}
		}
		GL11.glEnable(GL11.GL_CULL_FACE);
	}

	private void changeDamageOnBlock(Message message) {
		BlockDamageChangeMessage blockMessage = (BlockDamageChangeMessage) message;
		int key = blockMessage.getBlockId();
		int value = blockMessage.getDamage();

		RenderableModelNode[] nodes = dynamicObjects.get(key);
		if (nodes != null) {
			for (RenderableModelNode node : nodes) {
				node.setUniform("damage", new IntegerShaderUniform(value));
			}
		}
	}

	private void addDestroyingBlockToList(Message message) {
		BlockDestroyingMessage blockMessage = (BlockDestroyingMessage) message;
		int blockId = blockMessage.getBlockId();
		Block block = Blocks.getBlockForId(blockId);
		RenderableModelNode[] nodes = block.createRenderableModelNodeForDamagedBlock();
		IntVector position = blockMessage.getPosition();
		for (RenderableModelNode node : nodes) {
			node.setPosition(new Vector3f(position.x * Block.SIZE, position.y * Block.SIZE,
					position.z * Block.SIZE));
			node.setUniform("damage", new FloatShaderUniform(0.0f));
		}
		dynamicObjects.put(blockMessage.getId(), nodes);
	}

	private void handleDeletedRenderComponent(Message message) {
		RenderComponentMessage renderMessage = (RenderComponentMessage) message;
		dynamicObjects.remove(renderMessage.getOwnerId());
	}

	private void handleNewRenderComponent(Message message) {
		RenderComponentMessage renderMessage = (RenderComponentMessage) message;
		String modelId = renderMessage.getRenderableNodes();
		if (modelId.equals("anim")) {
			System.out.println("STOP");
		}
		RenderableModelNode[] nodes = modelLoader.getRenderableModelNodes(modelId);
		dynamicObjects.put(renderMessage.getOwnerId(), nodes);
	}

	private void createActorFromInventoryItem(GameItem gameItem, Vector3f position) {
		int id = idGenerator.getNextId();

		Actor actor = new Actor(id);

		PhysicsComponent physicsComponent = new PhysicsComponent(gameItem.getSize());
		physicsComponent.setShouldApplyGravity(true);
		physicsComponent.setOwner(actor);
		physicsComponent.afterInit();
		actor.addComponent(PhysicsComponent.PHYSICS_COMPONENT_PROPERTY_NAME, physicsComponent);

		TransformComponent transformComponent = new TransformComponent();
		transformComponent.setOwner(actor);
		transformComponent.setPosition(position);
		transformComponent.setScale(GameItem.ITEM_SIZE);
		transformComponent.afterInit();
		actor.addComponent(TransformComponent.TRANSFORM_COMPONENT_NAME, transformComponent);

		PickUpMovingComponent pickupMovingComponent = new PickUpMovingComponent(new InventoryItem(
				gameItem.getId(), 1));
		pickupMovingComponent.setOwner(actor);
		pickupMovingComponent.afterInit();
		actor.addComponent(PickUpMovingComponent.PICKUP_MOVING_COMPONENT_NAME,
				pickupMovingComponent);

		String modelId = gameItem.getModels();

		ModelRenderComponent modelRenderComponent = new ModelRenderComponent(modelId);
		modelRenderComponent.setOwner(actor);
		modelRenderComponent.afterInit();
		actor.addComponent(ModelRenderComponent.MODEL_RENDER_COMPONENT_NAME, modelRenderComponent);

		messager.sendMessage(new ActorLifeMessage(MessageTypes.NEW_ACTOR_MESSAGE,
				Message.MESSAGE_TARGET_ANYONE, actor));
	}

	void dropItems(Block block, Vector3f position) {
		List<DroppableItem> possibleDroppableItems = block.getPossibleDroppableItems();

		for (DroppableItem droppableItem : possibleDroppableItems) {
			float chance = droppableItem.getChance();
			int maxNumber = droppableItem.getMaxDrop();
			InventoryItem inventoryItem = droppableItem.getInventoryItem();
			GameItem gameItem = gameItems.getById(inventoryItem.getId());
			for (int i = 0; i < maxNumber; ++i) {
				if (random.nextFloat() < chance) {
					createActorFromInventoryItem(gameItem, position);
				}
			}
		}
	}
}
