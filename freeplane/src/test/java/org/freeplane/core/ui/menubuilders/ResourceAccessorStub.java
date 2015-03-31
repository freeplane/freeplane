package org.freeplane.core.ui.menubuilders;

import java.net.URL;

import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;

public class ResourceAccessorStub implements
		ResourceAccessor {
	@Override
	public String getProperty(String key) {
		return null;
	}

	@Override
	public String getRawText(String name) {
		return "text";
	}

	@Override
	public URL getResource(String name) {
		return null;
	}

	@Override
	public int getIntProperty(String name, int defaultValue) {
		return defaultValue;
	}
}