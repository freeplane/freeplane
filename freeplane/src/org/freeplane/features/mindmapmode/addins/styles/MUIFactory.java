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
package org.freeplane.features.mindmapmode.addins.styles;

import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.INodeSelectionListener;
import org.freeplane.core.frame.IMapSelectionListener;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.common.addins.styles.LogicalStyleController;
import org.freeplane.features.common.addins.styles.LogicalStyleModel;
import org.freeplane.features.common.addins.styles.MapStyle;
import org.freeplane.features.common.addins.styles.MapStyleModel;
import org.freeplane.features.common.map.IMapChangeListener;
import org.freeplane.features.common.map.INodeChangeListener;
import org.freeplane.features.common.map.MapChangeEvent;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeChangeEvent;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.nodestyle.MNodeStyleController;

public class MUIFactory implements INodeSelectionListener, INodeChangeListener, IMapChangeListener, IMapSelectionListener {
	private static final String[] sizes = { "8", "10", "12", "14", "16", "18", "20", "24", "28" };
	final private Controller controller;
	private boolean ignoreChangeEvent = false;
	final private DefaultComboBoxModel fonts, size, styles;
	private final MModeController modeController;

	public MUIFactory(final MModeController modeController) {
		this.modeController = modeController;
		size = new DefaultComboBoxModel(MUIFactory.sizes);
		styles = new DefaultComboBoxModel();
		final MNodeStyleController styleController = (MNodeStyleController) modeController.getExtension(NodeStyleController.class);
		controller = modeController.getController();
		final GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final String[] envFonts = gEnv.getAvailableFontFamilyNames();
		fonts = new DefaultComboBoxModel(envFonts);
		final ListDataListener fontsListener = new ListDataListener() {
			
			public void intervalRemoved(ListDataEvent e) {
			}
			
			public void intervalAdded(ListDataEvent e) {
			}
			
			public void contentsChanged(ListDataEvent e) {
				if (e.getIndex0() != -1) {
					return;
				}
				if (ignoreChangeEvent) {
					return;
				}
				ignoreChangeEvent = true;
				DefaultComboBoxModel source = (DefaultComboBoxModel) e.getSource();
				styleController.setFontFamily((String) source.getSelectedItem());
				ignoreChangeEvent = false;
			}
		};
		fonts.addListDataListener(fontsListener);
		final ListDataListener sizeListener = new ListDataListener() {
				public void intervalRemoved(ListDataEvent e) {
				}
				
				public void intervalAdded(ListDataEvent e) {
				}
				
				public void contentsChanged(ListDataEvent e) {
					if (e.getIndex0() != -1) {
						return;
					}
					if (ignoreChangeEvent) {
						return;
					}
				try {
					DefaultComboBoxModel source = (DefaultComboBoxModel) e.getSource();
	                final int intSize = Integer.parseInt(((String) source.getSelectedItem()));
	                styleController.setFontSize(intSize);
                }
                catch (NumberFormatException nfe) {
                }
			}
		};
		size.addListDataListener(sizeListener);
		
		final ListDataListener styleListener = new ListDataListener() {
			public void intervalRemoved(ListDataEvent e) {
			}
			
			public void intervalAdded(ListDataEvent e) {
			}
			
			public void contentsChanged(ListDataEvent e) {
				if (e.getIndex0() != -1) {
					return;
				}
				if (ignoreChangeEvent) {
					return;
				}
				DefaultComboBoxModel source = (DefaultComboBoxModel) e.getSource();
				final Object style = source.getSelectedItem();
				MLogicalStyleController controller = (MLogicalStyleController) modeController.getExtension(LogicalStyleController.class);
				controller.setStyle(style);
			}
		};
		styles.addListDataListener(styleListener);
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
		fonts.setSelectedItem(fontName);
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

	public Container createStyleBox() {
		JComboBox stylesBox = new JComboBox(styles);
		return stylesBox;
	}

	public Container createSizeBox() {
		JComboBox sizeBox = new JComboBox(size);
		sizeBox.setEditor(new BasicComboBoxEditor());
		sizeBox.setEditable(true);
		return sizeBox;
	}

	public Container createFontBox() {
		JComboBox fontsBox = new JComboBox(fonts);
		fontsBox.setMaximumRowCount(9);
		return fontsBox;
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
		styles.removeAllElements();
		if(newMap == null){
			return;
		}
		Collection<Object> styleObjects = MapStyleModel.getExtension(newMap).getStyles();
		for(Object style:styleObjects){
			styles.addElement(style);
		}
		ignoreChangeEvent = false;
	}

	public void afterMapClose(MapModel oldMap) {
	}

	public void beforeMapChange(MapModel oldMap, MapModel newMap) {
	}
}
