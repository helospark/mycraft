package com.helospark.mycraft.mycraft.window;

import java.util.Comparator;

public class DelayedMessage {
	static class DelayedMessageComparator implements Comparator<DelayedMessage> {

		@Override
		public int compare(DelayedMessage lhs, DelayedMessage rhs) {
			return (int) (lhs.deliveryTime - rhs.deliveryTime);
		}

	}

	private long deliveryTime;
	Message message;

	public DelayedMessage(Message message, long deliveryTime) {
		this.message = message;
		this.deliveryTime = deliveryTime;
	}

	public boolean shouldDeliverMessage(long currentTime) {
		return (currentTime >= deliveryTime);
	}

	public Message getMessage() {
		return message;
	}

}
