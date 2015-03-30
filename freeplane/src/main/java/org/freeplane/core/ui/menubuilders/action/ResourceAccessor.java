package org.freeplane.core.ui.menubuilders.action;

import java.net.URL;
public interface ResourceAccessor {

	public String getProperty(final String key);

	public String getRawText(String name);

	public URL getResource(final String name);

	public int getIntProperty(final String name, final int defaultValue);
}
