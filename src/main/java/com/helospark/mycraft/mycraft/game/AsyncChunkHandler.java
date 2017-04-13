package com.helospark.mycraft.mycraft.game;

import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;

@Service
public class AsyncChunkHandler implements MessageListener, Runnable {
	@Autowired
	private ChunkGenerator chunkGenerator;
	@Autowired
	private ChunkOptimizer chunkOptimizer;

	private MessageHandler messager;
	private LinkedBlockingQueue<Message> queue = new LinkedBlockingQueue<>();
	private volatile boolean running = true;
	private Thread thread = new Thread(this);

	public AsyncChunkHandler() {
		ApplicationContext context = Singleton.getInstance().getContext();
		messager = context.getBean(MessageHandler.class);
		messager.registerListener(this, MessageTypes.ACTOR_POSITION_CHANGED);
		messager.registerListener(this, MessageTypes.ESCAPE_REQUESTED);
		messager.registerListener(this, MessageTypes.GROW_TREE);
	}

	public void start() {
		thread.start();
	}

	@Override
	public boolean receiveMessage(Message message) {
		if (message.getType() == MessageTypes.ESCAPE_REQUESTED) {
			running = false;
			thread.interrupt();
		} else {
			queue.add(message);
		}
		return false;
	}

	@Override
	public void run() {
		while (running) {
			Message message = null;
			try {
				message = queue.take();
			} catch (InterruptedException e) {
				// Thread could be interrupted, while exiting, application,
				// don't throw, but check the status of running again
				continue;
			}
			chunkGenerator.receiveMessage(message);
		}
	}

}
