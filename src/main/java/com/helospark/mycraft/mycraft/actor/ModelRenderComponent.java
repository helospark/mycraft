package com.helospark.mycraft.mycraft.actor;

import org.springframework.context.ApplicationContext;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.messages.RenderComponentMessage;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class ModelRenderComponent extends ActorComponent implements
		MessageListener {

	public static final String MODEL_RENDER_COMPONENT_NAME = "RenderComponent";
	private String modelId;
	private MessageHandler messager;
	private boolean depthBuffer = true;

	public ModelRenderComponent(String modelId) {
		super(MODEL_RENDER_COMPONENT_NAME);
		this.modelId = modelId;
		ApplicationContext context = Singleton.getInstance().getContext();
		messager = context.getBean(MessageHandler.class);
	}

	@Override
	public Object createFromXML(Element node) {
		return null;
	}

	@Override
	public boolean receiveMessage(Message message) {
		return false;
	}

	@Override
	public void afterInit() {
		addModelToScene();
	}

	private void addModelToScene() {
		sendAddedModelMessageFromScene();
	}

	private void sendAddedModelMessageFromScene() {
		RenderComponentMessage renderMessage = new RenderComponentMessage(
				MessageTypes.NEW_RENDER_COMPONENT,
				Message.MESSAGE_TARGET_ANYONE, modelId, owner.id);
		messager.sendMessage(renderMessage);
	}

	@Override
	public void update(double deltaTime) {

	}

	@Override
	public void onRemove() {
		RenderComponentMessage renderMessage = new RenderComponentMessage(
				MessageTypes.DELETED_RENDER_COMPONENT,
				Message.MESSAGE_TARGET_ANYONE, modelId, owner.id);
		messager.sendImmediateMessage(renderMessage);
	}

	@Override
	public ActorComponent createNew() {
		return new ModelRenderComponent(modelId);
	}

	public String getModels() {
		return modelId;
	}

	public void changeModel(String models) {
		addModelToScene();
		this.modelId = models;
		sendAddedModelMessageFromScene();
	}

	public void setDepthBuffer(boolean b) {
		this.depthBuffer = b;
	}

	public boolean hasDepthBuffer() {
		return depthBuffer;
	}

}
