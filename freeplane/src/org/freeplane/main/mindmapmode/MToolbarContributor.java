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

import java.awt.GraphicsEnvironment;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.IMapSelectionListener;
import org.freeplane.core.modecontroller.IMapChangeListener;
import org.freeplane.core.modecontroller.INodeChangeListener;
import org.freeplane.core.modecontroller.INodeSelectionListener;
import org.freeplane.core.modecontroller.MapChangeEvent;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.modecontroller.NodeChangeEvent;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.common.addins.mapstyle.LogicalStyleController;
import org.freeplane.features.common.addins.mapstyle.LogicalStyleModel;
import org.freeplane.features.common.addins.mapstyle.MapStyle;
import org.freeplane.features.common.addins.mapstyle.MapStyleModel;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.addins.mapstyle.MLogicalStyleController;
import org.freeplane.features.mindmapmode.nodestyle.MNodeStyleController;

class MToolbarContributor implements IMenuContributor, INodeSelectionListener, INodeChangeListener, IMapChangeListener, IMapSelectionListener {
	private static final String[] sizes = { "8", "10", "12", "14", "16", "18", "20", "24", "28" };
	final private Controller controller;
	private boolean ignoreChangeEvent = false;
	final private JComboBox fonts, size, styles;
	private final MModeController modeController;

	public MToolbarContributor(final MModeController modeController) {
		this.modeController = modeController;
		final MNodeStyleController styleController = (MNodeStyleController) modeController.getExtension(NodeStyleController.class);
		controller = modeController.getController();
		final GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final String[] envFonts = gEnv.getAvailableFontFamilyNames();
		fonts = new JComboBox(envFonts);
		final ItemListener fontsListener = new ItemListener() {
			public void itemStateChanged(final ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED) {
					return;
				}
				if (ignoreChangeEvent) {
					return;
				}
				ignoreChangeEvent = true;
				styleController.setFontFamily((String) e.getItem());
				ignoreChangeEvent = false;
			}
		};
		fonts.addItemListener(fontsListener);
		fonts.setMaximumRowCount(9);
		size = new JComboBox(MToolbarContributor.sizes);
		final ItemListener sizeListener = new ItemListener() {
			public void itemStateChanged(final ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED) {
					return;
				}
				if (ignoreChangeEvent) {
					return;
				}
				try {
	                final int intSize = Integer.parseInt(((String) e.getItem()));
	                styleController.setFontSize(intSize);
                }
                catch (NumberFormatException nfe) {
                }
			}
		};
		size.addItemListener(sizeListener);
		size.setEditor(new BasicComboBoxEditor());
		size.setEditable(true);
		
		styles = new JComboBox();
		final ItemListener styleListener = new ItemListener() {
			public void itemStateChanged(final ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED) {
					return;
				}
				if (ignoreChangeEvent) {
					return;
				}
				final Object style = e.getItem();
				MLogicalStyleController controller = (MLogicalStyleController) modeController.getExtension(LogicalStyleController.class);
				controller.setStyle(style);
			}
		};
		styles.addItemListener(styleListener);
	}

	private void changeToolbar(final NodeModel node) {
		final MNodeStyleController styleController = (MNodeStyleController) modeController.getExtension(NodeStyleController.class);
		selectFontSize(Integer.toString(styleController.getFontSize(node)));
		selectFontName(styleController.getFontFamilyName(node));
		selectStyle(LogicalStyleModel.getStyle(node));
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
		if (ignoreChangeEvent) {
			return;
		}
		ignoreChangeEvent = true;
		fonts.setEditable(true);
		fonts.setSelectedItem(fontName);
		fonts.setEditable(false);
		ignoreChangeEvent = false;
	}

	private void selectFontSize(final String fontSize) {
		ignoreChangeEvent = true;
		size.setSelectedItem(fontSize);
		ignoreChangeEvent = false;
	}

	private void selectStyle(Object style) {
		ignoreChangeEvent = true;
		styles.setSelectedItem(style);
		ignoreChangeEvent = false;
	}

	public void updateMenus(final MenuBuilder builder) {
		AFreeplaneAction action = modeController.getAction("IncreaseNodeFontAction");
		builder.addComponent("/main_toolbar/font", fonts, action, MenuBuilder.AS_CHILD);
		builder.addComponent("/main_toolbar/font", size, action, MenuBuilder.AS_CHILD);
		builder.addComponent("/main_toolbar/font", styles, action, MenuBuilder.AS_CHILD);
	}

	public void mapChanged(MapChangeEvent event) {
		final Object property = event.getProperty();
		if (property.equals(MapStyle.MAP_STYLES)) {
			updateMapStyles(event.getMap());
			changeToolbar(controller.getSelection().getSelected());
			return;
		}
    }

	public void onNodeDeleted(NodeModel parent, NodeModel child, int index) {
   }

	public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
   }

	public void onNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
    }

	public void onPreNodeDelete(NodeModel oldParent, NodeModel selectedNode, int index) {
    }

	public void onPreNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
    }

	public void afterMapChange(MapModel oldMap, MapModel newMap) {
		updateMapStyles(newMap);
	}

	private void updateMapStyles(MapModel newMap) {
		ignoreChangeEvent = true;
		DefaultComboBoxModel model = (DefaultComboBoxModel) styles.getModel();
		model.removeAllElements();
		if(newMap == null){
			return;
		}
		Collection<Object> styleObjects = MapStyleModel.getExtension(newMap).getStyles();
		for(Object style:styleObjects){
			model.addElement(style);
		}
		ignoreChangeEvent = false;
	}

	public void afterMapClose(MapModel oldMap) {
	}

	public void beforeMapChange(MapModel oldMap, MapModel newMap) {
	}
}
