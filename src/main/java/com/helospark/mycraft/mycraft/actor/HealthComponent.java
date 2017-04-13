package com.helospark.mycraft.mycraft.actor;

import org.springframework.context.ApplicationContext;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.message.ActorDiedMessage;
import com.helospark.mycraft.mycraft.messages.ActorLifeDecreaseMessage;
import com.helospark.mycraft.mycraft.messages.GenericIntMessage;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

public class HealthComponent extends ActorComponent implements MessageListener {

    public static final String HEALTH_COMPONENT_NAME = "HealthComponent";
    public static final float HURT_TIME = 0.2f;
    private float timeLeftHurt = 0.0f;
    private int lifes;
    private final MessageHandler messager;
    private boolean isCurrentlyHurt = false;

    public HealthComponent(int lifes) {
        super(HEALTH_COMPONENT_NAME);
        this.lifes = lifes;
        ApplicationContext context = Singleton.getInstance().getContext();
        messager = context.getBean(MessageHandler.class);
        messager.registerListener(this, MessageTypes.ACTOR_LIFE_DECREASE);
    }

    @Override
    public Object createFromXML(Element node) {
        return null;
    }

    @Override
    public void afterInit() {
        Message message = new GenericIntMessage(MessageTypes.HEALTH_COMPONENT_CREATED,
                Message.MESSAGE_TARGET_ANYONE, owner.id);
        messager.sendMessage(message);
    }

    @Override
    public void update(double deltaTime) {
        if (isCurrentlyHurt) {
            timeLeftHurt -= deltaTime;
            if (timeLeftHurt <= 0) {
                isCurrentlyHurt = false;
            }
        }
    }

    @Override
    public void onRemove() {
        Message message = new GenericIntMessage(MessageTypes.HEALTH_COMPONENT_REMOVED,
                Message.MESSAGE_TARGET_ANYONE, owner.id);
        messager.sendMessage(message);
    }

    @Override
    public ActorComponent createNew() {
        return new HealthComponent(lifes);
    }

    @Override
    public boolean receiveMessage(Message message) {
        if (message.getType() == MessageTypes.ACTOR_LIFE_DECREASE) {
            ActorLifeDecreaseMessage actorMessage = (ActorLifeDecreaseMessage) message;
            if (actorMessage.getActorId() == owner.id) {
                lifes -= actorMessage.getAmount();
                isCurrentlyHurt = true;
                timeLeftHurt = HURT_TIME;
                if (lifes <= 0) {
                    ActorDiedMessage actorDiedMessage = new ActorDiedMessage(
                            MessageTypes.ACTOR_DIED_MESSAGE, owner.id);
                    messager.sendMessage(actorDiedMessage);
                }
            }
        }
        return false;
    }

    public int getLifes() {
        return lifes;
    }

    public boolean isCurrentlyHurt() {
        return isCurrentlyHurt;
    }

}
