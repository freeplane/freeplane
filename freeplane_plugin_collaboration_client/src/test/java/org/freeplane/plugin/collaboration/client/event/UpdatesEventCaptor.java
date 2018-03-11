package org.freeplane.plugin.collaboration.client.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.freeplane.collaboration.event.MapUpdated;
import org.freeplane.plugin.collaboration.client.event.batch.UpdatesProcessor;

public class UpdatesEventCaptor implements UpdatesProcessor {
	
	public static final int EVENT_TIMEOUT_MILLISECONDS = 500;

	
	private final CountDownLatch lock;
	private ArrayList<List<MapUpdated>> events;

	public UpdatesEventCaptor(int expectedEventCount) {
		this.lock = new CountDownLatch(expectedEventCount);
		events = new ArrayList<>();
	}

	@Override
	public void onUpdates(List<MapUpdated> updateBlock) {
		events.add(updateBlock);
		assertThat(lock.getCount() > 0);
		lock.countDown();
	}

	public List<List<MapUpdated>> getEvents()  throws InterruptedException {
		return getEventLists(EVENT_TIMEOUT_MILLISECONDS * lock.getCount(), TimeUnit.MILLISECONDS);
	}

	public List<MapUpdated> getEventList()  throws InterruptedException {
		return getEventList(EVENT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
	}

	public List<List<MapUpdated>> getEventLists(long timeout, TimeUnit unit)  throws InterruptedException {
		await(timeout, unit);
		return events;
	}

	public List<MapUpdated> getEventList(long timeout, TimeUnit unit)  throws InterruptedException {
		await(timeout, unit);
		assertThat(events).hasSize(1);
		return events.get(0);
	}
	
	private void await(long timeout, TimeUnit unit) throws InterruptedException {
		assertThat(lock.await(timeout, unit)).isTrue();
	}
}