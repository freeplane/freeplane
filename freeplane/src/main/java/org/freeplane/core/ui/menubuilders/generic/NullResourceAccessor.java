package org.freeplane.core.ui.menubuilders.generic;

import java.net.URL;

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
}