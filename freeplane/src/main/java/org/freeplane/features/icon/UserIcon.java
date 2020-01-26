/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Tamas Eppel
 *
 *  This file author is Tamas Eppel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.icon;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

/**
 *
 * Represents all icons used in Freeplane
 *
 * @author Tamas Eppel
 *
 */
public class UserIcon extends MindIcon {
	private static final String DEFAULT_IMAGE_PATH = //
	        ResourceController.getResourceController().getFreeplaneUserDirectory() + "/icons";

    public UserIcon(final String name, final String fileName, final String description, int order) {
		super(name, fileName, description, order);
	}

    @Override
    public String getImagePath() {
        return DEFAULT_IMAGE_PATH;
    }

    @Override
    public URL getUrl() {
        URL result = null;
        final String urlString = getPath();
        try {
            result = new File(urlString).toURI().toURL();
        }
        catch (final MalformedURLException e) {
            LogUtils.warn(String.format("could not create URL from [%s]", urlString));
        }
        return result;
    }
    
	@Override
	public String getTranslatedDescription() {
		String key = getDescriptionTranslationKey();
		return TextUtils.getOptionalText("usericon_" + key, key);
	}

    @Override
    public boolean hasStandardSize() {
        return false;
    }
	
	
}
