package com.helospark.mycraft.mycraft.network;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Configuration;

import com.helospark.mycraft.mycraft.messages.ActorPositionChangedMessage;
import com.helospark.mycraft.mycraft.messages.BlockDestroyedMessage;
import com.helospark.mycraft.mycraft.messages.ConnectToServerMessage;
import com.helospark.mycraft.mycraft.messages.GenericIntegerVectorMessage;
import com.helospark.mycraft.mycraft.messages.NewBlockMessage;
import com.helospark.mycraft.mycraft.messages.RenderComponentMessage;
import com.helospark.mycraft.mycraft.messages.UserConnectionResultMessage;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageTypes;

@Configuration
public class NetworkMessagesList {

	Map<MessageTypes, Message> typeToMessage = new HashMap<>();

	public NetworkMessagesList() {
		// @formatter:off
		typeToMessage.put(MessageTypes.ACTOR_POSITION_CHANGED,          new ActorPositionChangedMessage(MessageTypes.ACTOR_POSITION_CHANGED));
		typeToMessage.put(MessageTypes.NEW_RENDER_COMPONENT,            new RenderComponentMessage(MessageTypes.NEW_RENDER_COMPONENT));
//		typeToMessage.put(MessageTypes.NEW_ACTOR_MESSAGE,               new ActorLifeMessage(MessageTypes.NEW_ACTOR_MESSAGE));
		typeToMessage.put(MessageTypes.REMOVE_RENDER_COMPONENT,	        new RenderComponentMessage(MessageTypes.REMOVE_RENDER_COMPONENT));
		typeToMessage.put(MessageTypes.BLOCK_DESTROYED_MESSAGE,			new BlockDestroyedMessage(MessageTypes.BLOCK_DESTROYED_MESSAGE));
		typeToMessage.put(MessageTypes.ACTOR_ROTATION_CHANGED,			new ActorPositionChangedMessage(MessageTypes.ACTOR_ROTATION_CHANGED));
		typeToMessage.put(MessageTypes.BLOCK_CREATED_MESSAGE,			new NewBlockMessage(MessageTypes.BLOCK_CREATED_MESSAGE));
//		typeToMessage.put(MessageTypes.DELETED_ACTOR_MESSAGE,			new ActorLifeMessage(MessageTypes.DELETED_ACTOR_MESSAGE));
		typeToMessage.put(MessageTypes.DELETED_RENDER_COMPONENT,		new RenderComponentMessage(MessageTypes.DELETED_RENDER_COMPONENT));
		typeToMessage.put(MessageTypes.NEW_RENDER_COMPONENT,			new RenderComponentMessage(MessageTypes.NEW_RENDER_COMPONENT));	
//		typeToMessage.put(MessageTypes.BLOCK_DESTROYED_HANDLED,			new BlockDestroyedMessage(MessageTypes.BLOCK_DESTROYED_HANDLED));
//		typeToMessage.put(MessageTypes.BLOCK_DAMAGE_CHANGED_MESSAGE,	new BlockDamageChangeMessage(MessageTypes.BLOCK_DAMAGE_CHANGED_MESSAGE));
//		typeToMessage.put(MessageTypes.BLOCK_DAMAGE_CHANGED_MESSAGE,	new BlockDamageChangeMessage(MessageTypes.BLOCK_DAMAGE_CHANGED_MESSAGE));
//		typeToMessage.put(MessageTypes.BLOCK_DESTROYING_ENDED,			new BlockDestroyingMessage(MessageTypes.BLOCK_DESTROYING_ENDED));
//		typeToMessage.put(MessageTypes.BLOCK_DESTROYING_STARTED,		new BlockDestroyingMessage(MessageTypes.BLOCK_DESTROYING_STARTED));
		typeToMessage.put(MessageTypes.NEW_BLOCK_MESSAGE, 				new NewBlockMessage(MessageTypes.NEW_BLOCK_MESSAGE));
//		typeToMessage.put(MessageTypes.BLOCK_DESTRUCTION_HANDLED,		new GenericIntegerVectorMessage(MessageTypes.BLOCK_DESTRUCTION_HANDLED));
		typeToMessage.put(MessageTypes.BLOCK_PLACEMENT_HANDLED,			new GenericIntegerVectorMessage(MessageTypes.BLOCK_PLACEMENT_HANDLED));
		typeToMessage.put(MessageTypes.BLOCK_PLACEMENT_HANDLED,			new GenericIntegerVectorMessage(MessageTypes.BLOCK_PLACEMENT_HANDLED));
		typeToMessage.put(MessageTypes.SERVER_CONNECTION_REQUEST_MESSAGE, new ConnectToServerMessage(MessageTypes.SERVER_CONNECTION_REQUEST_MESSAGE));
		typeToMessage.put(MessageTypes.USER_CONNECTION_RESULT_MESSAGE,  new UserConnectionResultMessage(MessageTypes.USER_CONNECTION_RESULT_MESSAGE));
		// @formatter:on
	}

	public Map<MessageTypes, Message> getTypeToMessage() {
		return typeToMessage;
	}
}