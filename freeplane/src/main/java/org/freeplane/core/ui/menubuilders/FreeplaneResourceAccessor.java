package org.freeplane.core.ui.menubuilders;

import java.net.URL;

import javax.swing.Icon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.util.TextUtils;

public class FreeplaneResourceAccessor implements ResourceAccessor {
	@Override
	public URL getResource(final String key) {
		return ResourceController.getResourceController().getResource(key);
	}

	@Override
	public String getRawText(String key) {
		return TextUtils.getRawText(key);
	}

	@Override
	public String getProperty(final String key) {
		return ResourceController.getResourceController().getProperty(key, null);
	}

	@Override
	public int getIntProperty(String key, int defaultValue) {
		return ResourceController.getResourceController().getIntProperty(key, defaultValue);
	}

	@Override
	public String getText(String key) {
		return TextUtils.getText(key);
	}

	@Override
	public String getText(String key, String defaultValue) {
		return TextUtils.getText(key, defaultValue);
	}

    @Override
    public Icon getIcon(String key) {
        return ResourceController.getResourceController().getIcon(key);
    }
}