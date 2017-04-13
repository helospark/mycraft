package com.helospark.mycraft.mycraft.views;

import java.util.List;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.actor.Actor;
import com.helospark.mycraft.mycraft.actor.HealthComponent;
import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.messages.GenericIntMessage;
import com.helospark.mycraft.mycraft.render.SpriteAndTextBatchData;
import com.helospark.mycraft.mycraft.services.ActorSearchService;
import com.helospark.mycraft.mycraft.services.GlobalParameters;
import com.helospark.mycraft.mycraft.services.SpriteWriterService;
import com.helospark.mycraft.mycraft.shader.Shader;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class LifeShowingView extends View implements MessageListener {
	private static Vector3f START_POSITION;
	private static final IntVector FULL_HEALTH_POSITION = new IntVector(6, 0, 0);
	private static final IntVector HALF_HEALTH_POSITION = new IntVector(5, 0, 0);
	private static final Vector3f SCALE = new Vector3f(35, 35, 35);
	private MessageHandler messager;
	private HealthComponent healthComponent = null;
	private ActorSearchService actorSearchService;
	private SpriteWriterService spriteWriterService;
	private GlobalParameters globalParameters;

	// cache
	private Vector3f position = new Vector3f();

	public LifeShowingView(int id) {
		super(id);
		ApplicationContext context = Singleton.getInstance().getContext();
		messager = context.getBean(MessageHandler.class);
		messager.registerListener(this, MessageTypes.HEALTH_COMPONENT_CREATED);
		actorSearchService = context.getBean(ActorSearchService.class);
		spriteWriterService = context.getBean(SpriteWriterService.class);
		globalParameters = context.getBean(GlobalParameters.class);
		START_POSITION = new Vector3f(20, globalParameters.initialWindowHeight - 80, 0);
	}

	@Override
	public void fillBuffers(SpriteAndTextBatchData spriteTextBatchData) {
		if (healthComponent != null) {
			Shader shader = spriteTextBatchData
					.getShaderForId(SpriteAndTextBatchData.BATCH_TYPE_SPRITE);

			List<Float> dataList = spriteTextBatchData
					.getVertexListFor(SpriteAndTextBatchData.BATCH_TYPE_SPRITE);
			List<Integer> indexList = spriteTextBatchData
					.getIndexListFor(SpriteAndTextBatchData.BATCH_TYPE_SPRITE);

			position.x = START_POSITION.x;
			position.y = START_POSITION.y;
			position.z = START_POSITION.z;
			int index = 0;
			if (healthComponent.getLifes() > 0) {
				if (healthComponent.getLifes() >= 2) {
					for (index = 0; index < healthComponent.getLifes(); index += 2) {
						spriteWriterService.createBatchFromTexturePosition(FULL_HEALTH_POSITION,
								position, SCALE, dataList, shader, indexList);
						position.x += SCALE.x - 5;
					}
				}
				if (index > healthComponent.getLifes()) {
					spriteWriterService.createBatchFromTexturePosition(HALF_HEALTH_POSITION,
							position, SCALE, dataList, shader, indexList);
				}
			}
		}
	}

	@Override
	public boolean receiveMessage(Message message) {
		if (message.getType() == MessageTypes.HEALTH_COMPONENT_CREATED) {
			GenericIntMessage intMessage = (GenericIntMessage) message;
			Actor localPlayer = actorSearchService.getLocalHumanPlayer();
			if (intMessage.getParameter() == localPlayer.getId()) {
				healthComponent = (HealthComponent) localPlayer
						.getComponent(HealthComponent.HEALTH_COMPONENT_NAME);
				addView();
			}
		}
		return false;
	}
}
