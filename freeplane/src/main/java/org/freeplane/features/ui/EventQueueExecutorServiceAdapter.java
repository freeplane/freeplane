package org.freeplane.features.ui;

import java.awt.EventQueue;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;


class EventQueueExecutorServiceAdapter extends AbstractExecutorService {
	
	static final EventQueueExecutorServiceAdapter INSTANCE = new EventQueueExecutorServiceAdapter();
	
	private EventQueueExecutorServiceAdapter() {
		
	}

	@Override
	public void execute(Runnable task) {
		EventQueue.invokeLater(task);
	}

	@Override
	public void shutdown() {
		throw new IllegalStateException(
				"Manual shutdown not supported - ExecutorServiceAdapter is dependent on an external lifecycle");
	}

	@Override
	public List<Runnable> shutdownNow() {
		throw new IllegalStateException(
				"Manual shutdown not supported - ExecutorServiceAdapter is dependent on an external lifecycle");
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		throw new IllegalStateException(
				"Manual shutdown not supported - ExecutorServiceAdapter is dependent on an external lifecycle");
	}

	@Override
	public boolean isShutdown() {
		return false;
	}

	@Override
	public boolean isTerminated() {
		return false;
	}

}
