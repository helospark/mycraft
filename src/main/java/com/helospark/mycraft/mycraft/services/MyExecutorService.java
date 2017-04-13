package com.helospark.mycraft.mycraft.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

@Service
public class MyExecutorService {
	ExecutorService executorService = Executors
			.newFixedThreadPool(GlobalParameters.NUMBER_OF_EXECUTOR_THREADS);

	public ExecutorService getExecutorService() {
		return executorService;
	}
}
