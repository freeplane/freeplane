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

import java.net.MalformedURLException;
import java.net.URL;

import org.freeplane.core.resources.ResourceController;

public class EmojiIcon extends MindIcon {
    private static final String VERSION = "12.1.4";
    private static final String RESOURCE_IMAGE_PATH = "/images/emoji";
    private static final String REMOTE_IMAGE_PATH = "https://twemoji.maxcdn.com/v/" + VERSION + "/svg/";
    
    private static final boolean areResourcesAvailable = ResourceController.getResourceController().getResource(RESOURCE_IMAGE_PATH) != null;
    
    final String emoji;

    public EmojiIcon(final String emoji, final String entity, final String file, final String description, int order) {
		super(entityName(entity), file, description, order);
        this.emoji = emoji;
	}

    public static String entityName(final String entity) {
        return "emoji-" + entity.replace(' ', '-');
    }

    @Override
    public String getImagePath() {
        return RESOURCE_IMAGE_PATH;
    }
    
    
    
	@Override
    public URL getUrl() {
        if(! areResourcesAvailable)
            try {
                return new URL(REMOTE_IMAGE_PATH + getFile());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        else
            return super.getUrl();
    }

    @Override
	public String getTranslatedDescription() {
		String key = getDescriptionTranslationKey();
		return key;
	}

    @Override
    public boolean isShownOnToolbar() {
        return false;
    }
}
