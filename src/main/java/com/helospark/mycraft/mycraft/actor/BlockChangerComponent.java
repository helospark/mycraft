package com.helospark.mycraft.mycraft.actor;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.game.Block;
import com.helospark.mycraft.mycraft.game.Blocks;
import com.helospark.mycraft.mycraft.game.GameMap;
import com.helospark.mycraft.mycraft.game.RenderableBlock;
import com.helospark.mycraft.mycraft.mathutils.BoundingBox;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.messages.ActorPositionChangedMessage;
import com.helospark.mycraft.mycraft.messages.BlockDamageChangeMessage;
import com.helospark.mycraft.mycraft.messages.BlockDestroyedMessage;
import com.helospark.mycraft.mycraft.messages.BlockDestroyingMessage;
import com.helospark.mycraft.mycraft.messages.HitActorMessage;
import com.helospark.mycraft.mycraft.messages.MouseStateChangeMessage;
import com.helospark.mycraft.mycraft.raytracer.RayTracerResult;
import com.helospark.mycraft.mycraft.raytracer.RayTracerType;
import com.helospark.mycraft.mycraft.services.RayTracer;
import com.helospark.mycraft.mycraft.services.RayTracerProperty;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class BlockChangerComponent extends ActorComponent implements MessageListener {
    private static final float BLOCK_MAX_DAMAGE = 1.0f;
    public static final int MAX_BLOCK_DISTANCE = 5;
    public static final String BLOCK_CHANGE_COMPONENT_NAME = "BlockChangeComponent";
    private static final float DAMAGE_TEXTURE_COUNT = 10;
    private static final float BLOCK_CHANGE_DELTA_DAMAGE = BLOCK_MAX_DAMAGE / DAMAGE_TEXTURE_COUNT;
    private static final float RIGHT_CLICK_HANDLE_TIME = 0.15f;
    IntVector positionOfActiveBlock;
    boolean hasActiveBlock = false;
    Block activeBlock;
    float damage;
    float previousDamage = 0;
    MessageHandler messager;
    boolean isLeftMouseButtonDown = false;
    boolean isRightMouseButtonDown = false;
    RayTracer rayTracer;
    GameItems gameItems;
    GameMap gameMap;
    MyIdGenerator idGenerator;
    int idWithSomeName = -1;
    int activeBlockId;
    RayTracerResult rayTraceResult;
    private float lastRightClickHandled = 0;
    InventoryComponent inventory;

    public BlockChangerComponent() {
        super(BLOCK_CHANGE_COMPONENT_NAME);
        ApplicationContext context = Singleton.getInstance().getContext();
        messager = context.getBean(MessageHandler.class);
        messager.registerListener(this, MessageTypes.ACTOR_POSITION_CHANGED);
        messager.registerListener(this, MessageTypes.ACTOR_ROTATION_CHANGED);
        messager.registerListener(this, MessageTypes.BLOCK_DESTROYED_MESSAGE);
        messager.registerListener(this, MessageTypes.BLOCK_CREATED_MESSAGE);
        messager.registerListener(this, MessageTypes.KEY_STATE_CHANGE_MESSAGE);
        messager.registerListener(this, MessageTypes.MOUSE_MOTION_MESSAGE);
        messager.registerListener(this, MessageTypes.MOUSE_STATE_CHANGED_MESSAGE);
        rayTracer = context.getBean(RayTracer.class);
        gameItems = context.getBean(GameItems.class);
        gameMap = context.getBean(GameMap.class);
        idGenerator = context.getBean(MyIdGenerator.class);

    }

    @Override
    public boolean receiveMessage(Message message) {

        if (inventory == null) {
            inventory = (InventoryComponent) owner
                    .getComponent(InventoryComponent.INVENTORY_COMPONENT_NAME);
        }

        if (message.getType() == MessageTypes.MOUSE_STATE_CHANGED_MESSAGE) {
            MouseStateChangeMessage mouseMessage = (MouseStateChangeMessage) message;
            if (mouseMessage.getButton() == MouseStateChangeMessage.MOUSE_BUTTON_LEFT) {
                handleLeftClick(mouseMessage);
                if (isLeftMouseButtonDown) {
                    recalculateActiveBlock();
                }
            } else if (mouseMessage.getButton() == MouseStateChangeMessage.MOUSE_BUTTON_RIGHT) {

                handleRightClick(mouseMessage);
            }
        } else {
            if (message.getType() == MessageTypes.ACTOR_POSITION_CHANGED
                    || message.getType() == MessageTypes.ACTOR_ROTATION_CHANGED) {
                if (((ActorPositionChangedMessage) message).getActorId() != owner.id) {
                    return false;
                }
            }
        }
        return false;
    }

    private void handleRightClickPlacement() {
        if (!hasActiveBlock) {
            recalculateActiveBlock();

            if (!hasActiveBlock) {
                return;
            }
        }

        if (inventory == null) {
            throw new IllegalStateException("Cannot affect block without active inventory");
        }

        GameItem currentItem = inventory.getCurrentGameItem();
        if (activeBlock != null
                && activeBlock.handleRightClick(positionOfActiveBlock, currentItem.getId())) {
            isRightMouseButtonDown = false;
            resetBlock();
            return;
        }

        InventoryItem inventoryItem = inventory.getCurrentInventoryItem();
        GameItem gameItem = gameItems.getById(inventoryItem.getId());
        int side = rayTraceResult.getSide();
        IntVector sideVector = RenderableBlock.intVectorFromSide(side);
        boolean shouldDecrease = gameItem.applyEffectToBlock(activeBlock, positionOfActiveBlock,
                sideVector);
        if (shouldDecrease) {
            inventory.decreaseCurrentItem();
        }
        resetBlock();
    }

    private void recalculateActiveBlock() {
        TransformComponent transformComponent = (TransformComponent) owner
                .getComponent(TransformComponent.TRANSFORM_COMPONENT_NAME);
        Vector3f actorPosition = transformComponent.getPosition();
        // TODO: modify this with camera component
        actorPosition.x += 0.5f;
        actorPosition.y += 1.5f;
        actorPosition.z += 0.5f;
        Vector3f directionVector = transformComponent.getLookDirectionNormalVector();
        rayTraceResult = rayTracer.traceRays(actorPosition, directionVector, MAX_BLOCK_DISTANCE,
                new RayTracerProperty(new RayTracerType[] { RayTracerType.BLOCK, RayTracerType.ACTOR }, owner.id));
        if (rayTraceResult != null && rayTraceResult.hasFound()) {
            if (rayTraceResult.hasFoundBlock()) {
                handleFoundBlock(rayTraceResult, directionVector);
            } else {
                handleFoundActor(rayTraceResult, directionVector);
                isLeftMouseButtonDown = false;
                resetBlock();
                hasActiveBlock = false;
            }
        } else {
            resetBlock();
        }
    }

    private void handleFoundActor(RayTracerResult rayTraceResult, Vector3f directionVector) {
        Actor actor = rayTraceResult.getFoundActor();

        messager.sendMessage(new HitActorMessage(MessageTypes.HIT_ACTOR_MESSAGE, directionVector,
                10.0f, actor.getId(), owner.id));
    }

    private void handleFoundBlock(RayTracerResult result, Vector3f directionVector) {
        int blockId = result.getBlockId();
        Vector3f position = result.getPosition();
        position.x += directionVector.x * 0.001f;
        position.y += directionVector.y * 0.001f;
        position.z += directionVector.z * 0.001f;
        IntVector originalPosition = GameMap.getIntPositionFromPosition(position);

        // side of the block is ambiguous, add a little of the direction vector
        // to get inside of the block
        if (hasActiveBlock && originalPosition.equals(positionOfActiveBlock)) {
            destroyBlock(originalPosition);
        } else {
            resetBlock();
            hasActiveBlock = true;
            activeBlock = Blocks.getBlockForId(blockId);
            idWithSomeName = idGenerator.getNextId();
            positionOfActiveBlock = originalPosition;
            activeBlockId = blockId;
            damage = 0;
            sendBlockDestroyingStartedMessage(blockId);
        }
    }

    private void sendBlockDestroyingStoppedMessage() {
        BlockDestroyingMessage message = new BlockDestroyingMessage(
                MessageTypes.BLOCK_DESTROYING_ENDED, Message.MESSAGE_TARGET_ANYONE, activeBlockId,
                positionOfActiveBlock, idWithSomeName);
        messager.sendImmediateMessage(message);
    }

    private void sendBlockDestroyingStartedMessage(int blockId) {
        BlockDestroyingMessage message = new BlockDestroyingMessage(
                MessageTypes.BLOCK_DESTROYING_STARTED, Message.MESSAGE_TARGET_ANYONE, blockId,
                positionOfActiveBlock, idWithSomeName);
        messager.sendImmediateMessage(message);
    }

    private void destroyBlock(IntVector originalPosition) {

        GameItem currentItem = inventory.getCurrentGameItem();
        boolean willDrop = false;
        float blockDamage = currentItem.getStrength()
                * currentItem.getMultiplierForBlock(activeBlockId);
        if (activeBlock.canDestroyBlockWithItem(currentItem.getId())) {
            damage += blockDamage;
            willDrop = true;
        } else {
            damage += blockDamage / 10.0f;
        }
        sendBlockDamageChangedMessage(previousDamage);
        if (damage >= BLOCK_MAX_DAMAGE) {
            BoundingBox boundingBox = new BoundingBox(activeBlock.getBoundingBox());
            boundingBox.setPosition(positionOfActiveBlock.x * Block.SIZE, positionOfActiveBlock.y
                    * Block.SIZE, positionOfActiveBlock.z * Block.SIZE);
            messager.sendImmediateMessage(new BlockDestroyedMessage(
                    MessageTypes.BLOCK_DESTROYED_MESSAGE, Message.MESSAGE_TARGET_ANYONE,
                    originalPosition, activeBlockId, owner.getId(), boundingBox, willDrop));
            resetBlock();
        }
    }

    private void sendBlockDamageChangedMessage(float previousDamage) {
        float deltaDamage = damage - previousDamage;
        if (deltaDamage >= BLOCK_CHANGE_DELTA_DAMAGE || previousDamage == 0.0f) {
            int intDeltaDamage = (int) (deltaDamage / BLOCK_CHANGE_DELTA_DAMAGE);
            BlockDamageChangeMessage message = new BlockDamageChangeMessage(
                    MessageTypes.BLOCK_DAMAGE_CHANGED_MESSAGE, Message.MESSAGE_TARGET_ANYONE,
                    intDeltaDamage, idWithSomeName, owner.id);
            messager.sendImmediateMessage(message);
            previousDamage = damage;
        }
    }

    private void resetBlock() {
        if (hasActiveBlock) {
            sendBlockDestroyingStoppedMessage();
            hasActiveBlock = false;
            damage = 0;
            previousDamage = 0;
            positionOfActiveBlock = null;
        }
    }

    private void handleRightClick(MouseStateChangeMessage mouseMessage) {

        if (mouseMessage.getMouseEventType() == MouseStateChangeMessage.MOUSE_BUTTON_RELEASED) {
            isRightMouseButtonDown = false;
        } else {
            isRightMouseButtonDown = true;
        }
    }

    private void handleLeftClick(MouseStateChangeMessage mouseMessage) {
        if (mouseMessage.getMouseEventType() == MouseStateChangeMessage.MOUSE_BUTTON_RELEASED) {
            isLeftMouseButtonDown = false;
            damage = 0.0f;
            if (hasActiveBlock) {
                resetBlock();
            }
        } else {
            isLeftMouseButtonDown = true;
        }
    }

    @Override
    public Object createFromXML(Element node) {

        return null;
    }

    @Override
    public void afterInit() {

    }

    @Override
    public void update(double deltaTime) {

        if (isLeftMouseButtonDown) {
            recalculateActiveBlock();
        }
        if (isRightMouseButtonDown) {
            if (lastRightClickHandled > RIGHT_CLICK_HANDLE_TIME) {
                handleRightClickPlacement();
                lastRightClickHandled = 0.0f;
            }
        }
        lastRightClickHandled += (float) deltaTime;
        if (hasActiveBlock) {
            destroyBlock(positionOfActiveBlock);
        }
    }

    @Override
    public void onRemove() {

    }

    @Override
    public ActorComponent createNew() {
        return new BlockChangerComponent();
    }

}
