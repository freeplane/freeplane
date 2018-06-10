package org.freeplane.plugin.configurationservice;

import java.util.List;
import java.util.Map;

import org.freeplane.features.ui.ViewController;

class ThreadConfigurationSession {
	private final ConfigurationSession configurationSession;
	private final ViewController viewController;

	public ThreadConfigurationSession(ViewController viewController, ConfigurationSession configurationSession) {
		super();
		this.viewController = viewController;
		this.configurationSession = configurationSession;
	}

	public boolean isStarted() {
		return configurationSession.isStarted();
	}

	public void start(final String mindmapfile) {
		try {
			viewController.invokeAndWait(new Runnable() {
				
				@Override
				public void run() {
					configurationSession.start(mindmapfile);
					
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	void update(final String nodeId, final String attributeName, final int attributeValue) {
		try {
			viewController.invokeAndWait(new Runnable() {
				
				@Override
				public void run() {
					configurationSession.update(nodeId, attributeName, attributeValue);
					
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	Map<String, Object> values;
	public Map<String, Object> readValues(final String nodeId, final List<String> attributesList) {
		try {
			viewController.invokeAndWait(new Runnable() {
				
				@Override
				public void run() {
					values = configurationSession.readValues(nodeId, attributesList);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return values;
	}
	
	
}
