package com.helospark.mycraft.mycraft.actor;

import org.springframework.context.ApplicationContext;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.md5loader.Md5Model;
import com.helospark.mycraft.mycraft.render.RenderableModelNode;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;
import com.helospark.mycraft.mycraft.xml.ModelLoader;

public class AnimatedActorComponent extends ActorComponent implements MessageListener {

	public static final String ANIMATED_ACTOR_COMPONENT = "AnimatedActorComponent";
	private Md5Model md5Model;
	private ModelRenderComponent modelRenderComponent;
	private MessageHandler messager;
	private boolean isMoving = false;

	public AnimatedActorComponent(String animationFile) {
		super(ANIMATED_ACTOR_COMPONENT);
		ApplicationContext context = Singleton.getInstance().getContext();
		ModelLoader modelLoader = context.getBean(ModelLoader.class);
		md5Model = new Md5Model(animationFile);
		md5Model.load();
		md5Model.playAnimation("idle", true, false, false, 0.0f);
		md5Model.setPlaybackSpeed(1);
		md5Model.uploadVertexBuffers();
		RenderableModelNode[] nodes = md5Model.getRenderableNodes();
		modelLoader.addLoadedModel(animationFile, nodes);
		messager = context.getBean(MessageHandler.class);

		modelRenderComponent = new ModelRenderComponent(animationFile);
		messager.registerListener(this, MessageTypes.MOVE_IN_DIRECTION);
	}

	@Override
	public Object createFromXML(Element node) {
		return null;
	}

	@Override
	public void afterInit() {
		modelRenderComponent.setOwner(owner);
		modelRenderComponent.afterInit();
	}

	@Override
	public void update(double deltaTime) {
		md5Model.update(deltaTime);

		if (isMoving && !md5Model.getCurrentAnimationName().equals("walk")) {
			md5Model.playAnimation("walk", true, false, false, 0.0f);
		}
		if (!isMoving && !md5Model.getCurrentAnimationName().equals("idle")) {
			md5Model.playAnimation("idle", true, false, false, 0.0f);
		}
		isMoving = false;
	}

	@Override
	public void onRemove() {
		modelRenderComponent.onRemove();
	}

	@Override
	public ActorComponent createNew() {

		return null;
	}

	public ModelRenderComponent getModelRenderComponent() {
		return modelRenderComponent;
	}

	@Override
	public boolean receiveMessage(Message message) {
		if (message.getType() == MessageTypes.MOVE_IN_DIRECTION) {
			isMoving = true;
		}
		return false;
	}

}
