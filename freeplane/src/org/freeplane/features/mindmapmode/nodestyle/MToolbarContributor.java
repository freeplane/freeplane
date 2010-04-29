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
package org.freeplane.features.mindmapmode.nodestyle;

import java.awt.GraphicsEnvironment;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.INodeChangeListener;
import org.freeplane.core.modecontroller.INodeSelectionListener;
import org.freeplane.core.modecontroller.NodeChangeEvent;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;

class MToolbarContributor implements IMenuContributor, INodeSelectionListener, INodeChangeListener {
	private static final String[] sizes = { "8", "10", "12", "14", "16", "18", "20", "24", "28" };
	final private Controller controller;
	private boolean fontFamily_IgnoreChangeEvent = false;
	final private JComboBox fonts, size;
	private boolean fontSize_IgnoreChangeEvent = false;
	final private ItemListener fontsListener;
	final private ItemListener sizeListener;
	private final MNodeStyleController styleController;

	public MToolbarContributor(final MNodeStyleController styleController) {
		this.styleController = styleController;
		controller = styleController.getModeController().getController();
		final GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final String[] envFonts = gEnv.getAvailableFontFamilyNames();
		fonts = new JComboBox(envFonts);
		size = new JComboBox(MToolbarContributor.sizes);
		fontsListener = new ItemListener() {
			public void itemStateChanged(final ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED) {
					return;
				}
				if (fontFamily_IgnoreChangeEvent) {
					return;
				}
				fontFamily_IgnoreChangeEvent = true;
				styleController.setFontFamily((String) e.getItem());
				fontFamily_IgnoreChangeEvent = false;
			}
		};
		fonts.addItemListener(fontsListener);
		sizeListener = new ItemListener() {
			public void itemStateChanged(final ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED) {
					return;
				}
				if (fontSize_IgnoreChangeEvent) {
					return;
				}
				try {
					final int intSize = Integer.parseInt(((String) e.getItem()));
					styleController.setFontSize(intSize);
				}
				catch (final NumberFormatException nfe) {
				}
			}
		};
		size.addItemListener(sizeListener);
		fonts.setMaximumRowCount(9);
		size.setEditor(new BasicComboBoxEditor());
		size.setEditable(true);
	}

	private void changeToolbar(final NodeModel node) {
		selectFontSize(Integer.toString(styleController.getFontSize(node)));
		selectFontName(styleController.getFontFamilyName(node));
	}

	public void nodeChanged(final NodeChangeEvent event) {
		if (event.getNode() != controller.getSelection().getSelected()) {
			return;
		}
		changeToolbar(event.getNode());
	}

	public void onDeselect(final NodeModel node) {
	}

	public void onSelect(final NodeModel node) {
		changeToolbar(node);
	}

	private void selectFontName(final String fontName) {
		if (fontFamily_IgnoreChangeEvent) {
			return;
		}
		fontFamily_IgnoreChangeEvent = true;
		fonts.setEditable(true);
		fonts.setSelectedItem(fontName);
		fonts.setEditable(false);
		fontFamily_IgnoreChangeEvent = false;
	}

	private void selectFontSize(final String fontSize) {
		fontSize_IgnoreChangeEvent = true;
		size.setSelectedItem(fontSize);
		fontSize_IgnoreChangeEvent = false;
	}

	public void updateMenus(final MenuBuilder builder) {
		builder.addComponent("/main_toolbar/font", fonts, styleController.fontFamilyAction, MenuBuilder.AS_CHILD);
		builder.addComponent("/main_toolbar/font", size, styleController.fontSizeAction, MenuBuilder.AS_CHILD);
	}
}
