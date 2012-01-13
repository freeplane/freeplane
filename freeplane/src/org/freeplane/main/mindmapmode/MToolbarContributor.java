/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.main.mindmapmode;

import java.awt.Container;
import java.awt.Dimension;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.mindmapmode.MUIFactory;

class MToolbarContributor implements IMenuContributor {
	private final MUIFactory uiFactory;

	public MToolbarContributor( final MUIFactory uiFactory) {
		super();
//		this.modeController = modeController;
		this.uiFactory = uiFactory;
	}

// 	final private ModeController modeController;

	public void updateMenus(final ModeController modeController, final MenuBuilder builder) {
		final AFreeplaneAction action = modeController.getAction("IncreaseNodeFontAction");
		if(builder.contains("main_toolbar_font_name")){
			final Container fontBox = uiFactory.createFontBox();
			final Dimension preferredSize = fontBox.getPreferredSize();
			preferredSize.width = 90;
			fontBox.setPreferredSize(preferredSize);
			builder.addComponent("main_toolbar_font_name", fontBox, action, MenuBuilder.AS_CHILD);
		}
		if(builder.contains("main_toolbar_font_size")){
			final Container sizeBox = uiFactory.createSizeBox();
			builder.addComponent("main_toolbar_font_size", sizeBox, action, MenuBuilder.AS_CHILD);
		}
		if(builder.contains("main_toolbar_style")){
			final Container styleBox = uiFactory.createStyleBox();
			final Dimension preferredSize = styleBox.getPreferredSize();
			preferredSize.width = 90;
			styleBox.setPreferredSize(preferredSize);
			builder.addComponent("main_toolbar_style", styleBox, action, MenuBuilder.AS_CHILD);
		}
	}
}
