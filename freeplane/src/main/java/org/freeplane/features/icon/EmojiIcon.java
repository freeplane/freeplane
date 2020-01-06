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

public class EmojiIcon extends MindIcon {
    private static final String DEFAULT_IMAGE_PATH = "/images/emoji";
    final String emoji;
    private final boolean isShownOnToolbar;

    public EmojiIcon(final String emoji, final String entity, final String fileName, final String description, int order, boolean isShownOnToolbar) {
		super(entityName(entity), fileName, description, order);
        this.emoji = emoji;
        this.isShownOnToolbar = isShownOnToolbar;
	}

    public static String entityName(final String entity) {
        return "emoji-" + entity.replace(' ', '-');
    }

    @Override
    public String getImagePath() {
        return DEFAULT_IMAGE_PATH;
    }
    
	@Override
	public String getTranslatedDescription() {
		String key = getDescriptionTranslationKey();
		return key;
	}

    @Override
    public boolean isShownOnToolbar() {
        return isShownOnToolbar;
    }
}
