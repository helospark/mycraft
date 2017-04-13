package com.helospark.mycraft.mycraft.actor;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.messages.ActorLifeDecreaseMessage;
import com.helospark.mycraft.mycraft.messages.GenericIntMessage;
import com.helospark.mycraft.mycraft.messages.HitActorMessage;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class HittableActorComponent extends ActorComponent implements MessageListener {

	public static final String HITTABLE_ACTOR_COMPONENT_NAME = "HittableActorComponent";
	private MessageHandler messager;

	public HittableActorComponent() {
		super(HITTABLE_ACTOR_COMPONENT_NAME);
		ApplicationContext context = Singleton.getInstance().getContext();
		messager = context.getBean(MessageHandler.class);
		messager.registerListener(this, MessageTypes.HIT_ACTOR_MESSAGE);
	}

	@Override
	public Object createFromXML(Element node) {
		return new HittableActorComponent();
	}

	@Override
	public void afterInit() {
		messager.sendMessage(new GenericIntMessage(MessageTypes.NEW_HITTABLE_ACTOR_COMPONENT,
				Message.MESSAGE_TARGET_ANYONE, owner.id));
	}

	@Override
	public void update(double deltaTime) {

	}

	@Override
	public void onRemove() {
		messager.sendMessage(new GenericIntMessage(MessageTypes.REMOVED_HITTABLE_ACTOR_COMPONENT,
				Message.MESSAGE_TARGET_ANYONE, owner.id));
	}

	@Override
	public ActorComponent createNew() {
		return new HittableActorComponent();
	}

	@Override
	public boolean receiveMessage(Message message) {
		if (message.getType() == MessageTypes.HIT_ACTOR_MESSAGE) {
			HitActorMessage hitMessage = (HitActorMessage) message;
			if (hitMessage.getHitActor() == owner.id) {
				PhysicsComponent physicsComponent = (PhysicsComponent) owner
						.getComponent(PhysicsComponent.PHYSICS_COMPONENT_PROPERTY_NAME);
				Vector3f direction = new Vector3f(hitMessage.getDirection());
				// direction.y += 0.5f;
				VectorMathUtils.mul(direction, 0.2f);
				physicsComponent.addVelocity(direction);
				messager.sendMessage(new ActorLifeDecreaseMessage(MessageTypes.ACTOR_LIFE_DECREASE,
						owner.id, (int) hitMessage.getStrength()));
			}
		}
		return false;
	}

}
