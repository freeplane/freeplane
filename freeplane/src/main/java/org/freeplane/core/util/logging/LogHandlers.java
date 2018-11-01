package org.freeplane.core.util.logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Handler;

public class LogHandlers {
	private static final Collection<Handler> handlers = new ArrayList<>();

	public static Collection<Handler> getHandlers() {
		return handlers;
	}

	public static void addHandler(Handler handler) {
		handlers.add(handler);
	}

	public static void removeHandler(Handler handler) {
		handlers.remove(handler);
	}
}