package org.freeplane.main.application;

public interface ApplicationLifecycleListener {
	void onStartupFinished();
	void onApplicationStopped();
}
