package com.helospark.mycraft.mycraft.singleton;

import org.springframework.context.ApplicationContext;

public class Singleton {
	private ApplicationContext applicationContext;
	private static Singleton instance = new Singleton();

	public static Singleton getInstance() {
		return instance;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public ApplicationContext getContext() {
		return applicationContext;
	}
}
