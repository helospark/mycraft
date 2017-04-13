package com.helospark.mycraft.mycraft.actor;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.helpers.XmlHelpers;
import com.helospark.mycraft.mycraft.mathutils.VectorMathUtils;
import com.helospark.mycraft.mycraft.messages.ActorPositionChangeMessage;
import com.helospark.mycraft.mycraft.messages.ActorPositionChangedMessage;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class TransformComponent extends ActorComponent implements
		MessageListener {
	public static final String TRANSFORM_COMPONENT_NAME = "TransformComponent3D";
	private Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
	private Vector3f rotation = new Vector3f();
	private Vector3f scale = new Vector3f(1, 1, 1);
	private boolean isLocal = false;

	private MessageHandler messager;

	public TransformComponent() {
		super(TRANSFORM_COMPONENT_NAME);
		ApplicationContext context = Singleton.getInstance().getContext();
		messager = context.getBean(MessageHandler.class);
		messager.registerListener(this, MessageTypes.CHANGE_ACTOR_POSITION);
	}

	@Override
	public void update(double deltaTime) {

	}

	@Override
	public void onRemove() {

	}

	@Override
	public void afterInit() {
		sendPositionUpdateMessage(new Vector3f(position));
	}

	@Override
	public Object createFromXML(Element element) {
		if (!checkIfRightElement(element.getAttribute("type"))) {
			return new TransformComponent();
		}

		TransformComponent component = new TransformComponent();

		component.position = XmlHelpers.parseVector(element, "position");

		return component;
	}

	public void setPosition(Vector3f position) {
		Vector3f oldPosition = new Vector3f(this.position);
		this.position = new Vector3f(position);
		if (VectorMathUtils.distanceSquareBetween(position, oldPosition) > 0.001f) {
			sendPositionUpdateMessage(oldPosition);
		}
	}

	public void moveY(double d) {
		Vector3f oldPosition = new Vector3f(position);
		position.y += d;

		sendPositionUpdateMessage(oldPosition);
	}

	public void moveX(double d) {
		Vector3f oldPosition = new Vector3f(position);
		position.x += d;

		sendPositionUpdateMessage(oldPosition);
	}

	private void sendPositionUpdateMessage(Vector3f oldPosition) {
		ActorPositionChangedMessage message = new ActorPositionChangedMessage(
				MessageTypes.ACTOR_POSITION_CHANGED,
				Message.MESSAGE_TARGET_ANYONE, new Vector3f(position),
				oldPosition, owner.id);

		messager.sendImmediateMessage(message);
	}

	@Override
	public ActorComponent createNew() {
		TransformComponent component = new TransformComponent();
		component.setPosition(position);
		return component;
	}

	public Vector3f getPosition() {
		return position;
	}

	public Vector3f getPositionReference() {
		return position;
	}

	@Override
	public boolean receiveMessage(Message message) {
		if (message.getType() == MessageTypes.CHANGE_ACTOR_POSITION) {
			ActorPositionChangeMessage actorMessage = (ActorPositionChangeMessage) message;
			if (actorMessage.getActorId() == owner.id) {
				// System.out.println("Got position: "
				// + actorMessage.getNewPosition() + " " + owner.id);
				this.position = new Vector3f(actorMessage.getNewPosition());
			}
		}
		return false;
	}

	public void setDirection(float pitch, float yaw) {
		Vector3f oldDirection = new Vector3f(rotation);
		this.rotation.x = pitch;
		this.rotation.y = yaw;
		this.rotation.z = 0.0f;

		if (VectorMathUtils.distanceSquareBetween(oldDirection, rotation) > 0.001f) {
			sendDirectionUpdateMessage(oldDirection);
		}
	}

	private void sendDirectionUpdateMessage(Vector3f oldDirection) {
		ActorPositionChangedMessage message = new ActorPositionChangedMessage(
				MessageTypes.ACTOR_ROTATION_CHANGED,
				Message.MESSAGE_TARGET_ANYONE, rotation, oldDirection, owner.id);

		messager.sendMessage(message);
	}

	public Vector3f getDirection() {
		return rotation;
	}

	public Vector3f getLookDirectionNormalVector() {
		return VectorMathUtils.getLookVectorFromAngles(rotation.x, rotation.y);
	}

	public void setRotation(Vector3f newRotation) {
		Vector3f oldDirection = new Vector3f(rotation);
		this.rotation.x = newRotation.x;
		this.rotation.y = newRotation.y;
		this.rotation.z = newRotation.z;
		sendDirectionUpdateMessage(oldDirection);
	}

	public void setScale(Vector3f scale) {
		this.scale = scale;
	}

	public Vector3f getScale() {
		return scale;
	}

	public void setIsLocal(boolean b) {
		this.isLocal = b;
	}

	public boolean isLocal() {
		return isLocal;
	}
}
