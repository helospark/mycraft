package com.helospark.mycraft.mycraft;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.helospark.mycraft.mycraft.singleton.Singleton;

public class SingletonInitializeBean implements ApplicationContextAware {

	ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		context = applicationContext;
		Singleton.getInstance().setApplicationContext(context);
	}

}
