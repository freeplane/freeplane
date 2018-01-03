package org.freeplane.plugin.collaboration.client.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.freeplane.plugin.collaboration.client.event.batch.UpdateBlockCompleted;
import org.freeplane.plugin.collaboration.client.event.batch.UpdatesProcessor;

public class UpdatesEventCaptor implements UpdatesProcessor {
	
	private static final int EVENT_TIMEOUT_MILLISECONDS = 500;

	
	private final CountDownLatch lock;
	private ArrayList<UpdateBlockCompleted> events;

	public UpdatesEventCaptor(int expectedEventCount) {
		this.lock = new CountDownLatch(expectedEventCount);
		events = new ArrayList<>();
	}

	@Override
	public void onUpdates(UpdateBlockCompleted event) {
		events.add(event);
		assertThat(lock.getCount() > 0);
		lock.countDown();
	}

	public List<UpdateBlockCompleted> getEvents()  throws InterruptedException {
		return getEvents(EVENT_TIMEOUT_MILLISECONDS * lock.getCount(), TimeUnit.MILLISECONDS);
	}

	public UpdateBlockCompleted getEvent()  throws InterruptedException {
		return getEvent(EVENT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
	}

	public List<UpdateBlockCompleted> getEvents(long timeout, TimeUnit unit)  throws InterruptedException {
		await(timeout, unit);
		return events;
	}

	public UpdateBlockCompleted getEvent(long timeout, TimeUnit unit)  throws InterruptedException {
		await(timeout, unit);
		assertThat(events).hasSize(1);
		return events.get(0);
	}
	
	private void await(long timeout, TimeUnit unit) throws InterruptedException {
		assertThat(lock.await(timeout, unit)).isTrue();
	}
}