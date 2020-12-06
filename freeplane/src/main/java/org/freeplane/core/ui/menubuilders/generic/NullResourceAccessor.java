package org.freeplane.core.ui.menubuilders.generic;

import java.net.URL;

import javax.swing.Icon;

class NullResourceAccessor implements ResourceAccessor {
    @Override
    public URL getResource(String name) {
    	return null;
    }

    @Override
    public String getRawText(String name) {
    	return null;
    }

    @Override
    public String getProperty(String key) {
    	return null;
    }

    @Override
    public int getIntProperty(String name, int defaultValue) {
    	return 0;
    }

	@Override
	public String getText(String name) {
		return null;
	}

	@Override
	public String getText(String name, String defaultValue) {
		return defaultValue;
	}

    @Override
    public Icon getIcon(String key) {
        return null;
    }

    @Override
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        return false;
    }
}