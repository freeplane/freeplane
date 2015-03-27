package org.freeplane.core.ui.menubuilders;

import java.net.URL;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.menubuilders.action.ResourceAccessor;
import org.freeplane.core.util.TextUtils;

class FreeplaneResourceAccessor implements ResourceAccessor {
	@Override
	public URL getResource(final String name) {
		return ResourceController.getResourceController().getResource(name);
	}

	@Override
	public String getRawText(String name) {
		return TextUtils.getRawText(name);
	}

	@Override
	public String getProperty(final String key) {
		return ResourceController.getResourceController().getProperty(key, null);
	}
}