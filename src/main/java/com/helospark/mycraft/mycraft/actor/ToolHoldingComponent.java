package com.helospark.mycraft.mycraft.actor;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.messages.ActiveInventoryItemChangedMessage;
import com.helospark.mycraft.mycraft.messages.ActorLifeMessage;
import com.helospark.mycraft.mycraft.messages.ActorPositionChangedMessage;
import com.helospark.mycraft.mycraft.messages.BlockDamageChangeMessage;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class ToolHoldingComponent extends ActorComponent implements
        MessageListener {

    private static final Vector3f ROTATION_DIRECTION = new Vector3f(0, 0, 10);
    private static int MOVE_AMOUNT_FRAME = 7;

    public static final String TOOL_HOLDING_COMPONENT_NAME = "ToolHoldingComponent";
    MessageHandler messager;
    GameItems items;
    Actor createdActor = null;
    MyIdGenerator idGenerator;
    boolean isMovingToolPositive = false;
    boolean isMovingToolNegative = false;
    int numFrames = 0;

    // cache
    Vector3f leftVector = new Vector3f();
    Vector3f tmpResult = new Vector3f();

    Vector3f rotationTmp = new Vector3f(-16.0f, 49.0f, 57.0f);

    public ToolHoldingComponent() {
        super(TOOL_HOLDING_COMPONENT_NAME);
        ApplicationContext context = Singleton.getInstance().getContext();
        items = context.getBean(GameItems.class);
        idGenerator = context.getBean(MyIdGenerator.class);
        messager = context.getBean(MessageHandler.class);
        messager.registerListener(this, MessageTypes.ACTOR_POSITION_CHANGED);
        messager.registerListener(this, MessageTypes.ACTOR_ROTATION_CHANGED);
        messager.registerListener(this,
                MessageTypes.INVENTORY_ACTIVE_ITEM_CHANGED);
        messager.registerListener(this,
                MessageTypes.BLOCK_DAMAGE_CHANGED_MESSAGE);
    }

    @Override
    public Object createFromXML(Element node) {
        return null;
    }

    @Override
    public void afterInit() {
        createdActor = new Actor(idGenerator.getNextId());
        TransformComponent transformComponent = new TransformComponent();
        transformComponent.setOwner(createdActor);
        transformComponent.afterInit();
        createdActor.addComponent(TransformComponent.TRANSFORM_COMPONENT_NAME,
                transformComponent);
        messager.sendMessage(new ActorLifeMessage(
                MessageTypes.NEW_ACTOR_MESSAGE, Message.MESSAGE_TARGET_ANYONE,
                createdActor));
    }

    @Override
    public void update(double deltaTime) {
        if (isMovingToolPositive) {
            VectorMathUtils.addInPlace(rotationTmp, ROTATION_DIRECTION);
            ++numFrames;
            if (numFrames >= MOVE_AMOUNT_FRAME) {
                isMovingToolNegative = true;
                isMovingToolPositive = false;
                numFrames = 0;
            }
        }
        if (isMovingToolNegative) {
            VectorMathUtils.subInPlace(rotationTmp, ROTATION_DIRECTION);
            ++numFrames;
            if (numFrames >= MOVE_AMOUNT_FRAME) {
                isMovingToolNegative = false;
                isMovingToolPositive = false;
                numFrames = 0;
            }
        }
    }

    @Override
    public void onRemove() {
        messager.sendMessage(new ActorLifeMessage(
                MessageTypes.DELETED_ACTOR_MESSAGE,
                Message.MESSAGE_TARGET_ANYONE, createdActor));
    }

    @Override
    public ActorComponent createNew() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean receiveMessage(Message message) {
        if (message.getType() == MessageTypes.ACTOR_POSITION_CHANGED) {
            ActorPositionChangedMessage actorPositionChangedMessage = (ActorPositionChangedMessage) message;
            if (actorPositionChangedMessage.getActorId() == owner.getId()) {
                recalculateHeldComponent();
            }
        } else if (message.getType() == MessageTypes.ACTOR_ROTATION_CHANGED) {
            ActorPositionChangedMessage actorRotationChanged = (ActorPositionChangedMessage) message;
            if (actorRotationChanged.getActorId() == owner.getId()) {
                recalculateHeldComponent();
            }
        } else if (message.getType() == MessageTypes.INVENTORY_ACTIVE_ITEM_CHANGED) {
            ActiveInventoryItemChangedMessage intMessage = (ActiveInventoryItemChangedMessage) message;
            if (intMessage.getActorId() == owner.getId()) {
                recalculateHeldComponent();
            }
        } else if (message.getType() == MessageTypes.BLOCK_DAMAGE_CHANGED_MESSAGE) {
            if (((BlockDamageChangeMessage) message).getOwnerId() != owner.id) {
                return false;
            }
            if (!isMovingToolPositive && !isMovingToolNegative) {
                isMovingToolPositive = true;
                isMovingToolNegative = false;
                numFrames = 0;
            }
        }
        return false;
    }

    private void recalculateHeldComponent() {
        InventoryComponent ownerInventoryComponent = (InventoryComponent) owner
                .getComponent(InventoryComponent.INVENTORY_COMPONENT_NAME);
        TransformComponent ownerTransformComponent = (TransformComponent) owner
                .getComponent(TransformComponent.TRANSFORM_COMPONENT_NAME);

        InventoryItem activeInventoryItem = ownerInventoryComponent
                .getCurrentInventoryItem();
        GameItem item = items.getById(activeInventoryItem.getId());
        String modelId = item.getModels();

        TransformComponent createdTransformComponent = (TransformComponent) createdActor
                .getComponent(TransformComponent.TRANSFORM_COMPONENT_NAME);
        ModelRenderComponent createdModelRenderComponent = (ModelRenderComponent) createdActor
                .getComponent(ModelRenderComponent.MODEL_RENDER_COMPONENT_NAME);

        if (createdModelRenderComponent == null) {
            createdModelRenderComponent = new ModelRenderComponent(modelId);
            createdModelRenderComponent.setDepthBuffer(false);
            createdModelRenderComponent.setOwner(createdActor);
            createdModelRenderComponent.afterInit();
            createdActor.addComponent(
                    ModelRenderComponent.MODEL_RENDER_COMPONENT_NAME,
                    createdModelRenderComponent);
        } else {
            if (!createdModelRenderComponent.getModels().equals(modelId)) {
                createdModelRenderComponent.changeModel(modelId);
            }
        }

        createdTransformComponent.setIsLocal(true);
        createdTransformComponent.setPosition(new Vector3f(1.4f, -0.4f, -3.5f));
        createdTransformComponent.setScale(new Vector3f(2, -2, 2));
        createdTransformComponent.setRotation(new Vector3f((float) Math
                .toRadians(rotationTmp.x), (float) Math
                        .toRadians(rotationTmp.y),
                (float) Math
                        .toRadians(rotationTmp.z)));
    }
}
