package org.docear.plugin.core;

import java.util.HashSet;

public class SemaphoreController {
	private final HashSet<String> semaphores = new HashSet<String>();

	public void lock(final String s) {
		synchronized (semaphores) {
			semaphores.add(s);
		}
	}

	public void unlock(final String s) {
		synchronized (semaphores) {
			semaphores.remove(s);
		}
	}

	public boolean isLocked(final String s) {
		synchronized (semaphores) {
			return semaphores.contains(s);
		}
	}

}
