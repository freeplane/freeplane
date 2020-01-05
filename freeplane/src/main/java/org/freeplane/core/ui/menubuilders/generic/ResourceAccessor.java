package org.freeplane.core.ui.menubuilders.generic;

import java.net.URL;

import javax.swing.Icon;
public interface ResourceAccessor {

	public static final ResourceAccessor NULL_RESOURCE_ACCESSOR = new NullResourceAccessor();

	public String getProperty(final String key);
	public Icon getIcon(String key);

	public String getRawText(String name);
	
	public String getText(String name);
	
	public String getText(String name, String defaultValue);

	public URL getResource(final String name);

	public int getIntProperty(final String name, final int defaultValue);

}
