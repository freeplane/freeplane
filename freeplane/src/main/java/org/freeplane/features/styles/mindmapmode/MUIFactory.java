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
package org.freeplane.features.styles.mindmapmode;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.util.Collection;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.FixedBasicComboBoxEditor;
import org.freeplane.core.ui.components.JComboBoxWithBorder;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapStyleModel;

public class MUIFactory implements INodeSelectionListener, INodeChangeListener, IMapChangeListener,
        IMapSelectionListener, IExtension {
// 	final private Controller controller;
	private boolean ignoreChangeEvent = false;
	final private DefaultComboBoxModel fonts, size, styles;
//  private final MModeController modeController;

	public DefaultComboBoxModel getStyles() {
    	return styles;
    }

	final static public String[] FONT_SIZES = new String[] { "2", "4", "6", "8", "10", "12", "14", "16", "18", "20", "22", "24",
	        "30", "36", "48", "72" };
	public MUIFactory() {
		size = new DefaultComboBoxModel(MUIFactory.FONT_SIZES);
		styles = new DefaultComboBoxModel();
		final ModeController modeController = Controller.getCurrentModeController();
		final MNodeStyleController styleController = (MNodeStyleController) modeController
		    .getExtension(NodeStyleController.class);
		final GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final String[] envFonts = gEnv.getAvailableFontFamilyNames();
		fonts = new DefaultComboBoxModel(envFonts);
		final ListDataListener fontsListener = new ListDataListener() {
			@Override
			public void intervalRemoved(final ListDataEvent e) {
			}

			@Override
			public void intervalAdded(final ListDataEvent e) {
			}

			@Override
			public void contentsChanged(final ListDataEvent e) {
				if (e.getIndex0() != -1) {
					return;
				}
				if (ignoreChangeEvent) {
					return;
				}
				ignoreChangeEvent = true;
				final DefaultComboBoxModel source = (DefaultComboBoxModel) e.getSource();
				styleController.setFontFamily((String) source.getSelectedItem());
				ignoreChangeEvent = false;
			}
		};
		fonts.addListDataListener(fontsListener);
		final ListDataListener sizeListener = new ListDataListener() {
			@Override
			public void intervalRemoved(final ListDataEvent e) {
			}

			@Override
			public void intervalAdded(final ListDataEvent e) {
			}

			@Override
			public void contentsChanged(final ListDataEvent e) {
				if (e.getIndex0() != -1) {
					return;
				}
				if (ignoreChangeEvent) {
					return;
				}
				try {
					final DefaultComboBoxModel source = (DefaultComboBoxModel) e.getSource();
					final int intSize = Integer.parseInt(((String) source.getSelectedItem()));
					styleController.setFontSize(intSize);
				}
				catch (final NumberFormatException nfe) {
				}
			}
		};
		size.addListDataListener(sizeListener);
		final ListDataListener styleListener = new ListDataListener() {
			@Override
			public void intervalRemoved(final ListDataEvent e) {
			}

			@Override
			public void intervalAdded(final ListDataEvent e) {
			}

			@Override
			public void contentsChanged(final ListDataEvent e) {
				if (e.getIndex0() != -1) {
					return;
				}
				if (ignoreChangeEvent) {
					return;
				}
				final DefaultComboBoxModel source = (DefaultComboBoxModel) e.getSource();
				final IStyle style = (IStyle) source.getSelectedItem();
				final MLogicalStyleController controller = (MLogicalStyleController) modeController
				    .getExtension(LogicalStyleController.class);
				controller.setStyle(style);
			}
		};
		styles.addListDataListener(styleListener);
	}

	private void changeToolbar(final NodeModel node) {
		final Controller controller = Controller.getCurrentController();
		final MNodeStyleController styleController = (MNodeStyleController) controller.getModeController()
		    .getExtension(NodeStyleController.class);
		selectFontSize(Integer.toString(styleController.getFontSize(node)));
		selectFontName(styleController.getFontFamilyName(node));
		final LogicalStyleController logicalStyleController = LogicalStyleController.getController();
		ignoreChangeEvent = true;
 		styles.setSelectedItem(logicalStyleController.getFirstStyle(node));
        ignoreChangeEvent = false;
	}

	@Override
	public void nodeChanged(final NodeChangeEvent event) {
		IMapSelection selection = Controller.getCurrentController().getSelection();
		if(selection != null) {
			if (event.getNode() != selection.getSelected()) {
				return;
			}
		}
		changeToolbar(event.getNode());
	}

	@Override
	public void onDeselect(final NodeModel node) {
	}

	@Override
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

	public Container createStyleBox() {
		final JComboBox stylesBox = new JComboBoxWithBorder(styles);
		stylesBox.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXX");
		stylesBox.setRenderer(new ComboBoxRendererWithTooltip(stylesBox));
		return stylesBox;
	}

	public Container createSizeBox() {
		final JComboBox sizeBox = new JComboBoxWithBorder(size);
		sizeBox.setPrototypeDisplayValue("88888");
		sizeBox.setPreferredSize(sizeBox.getPreferredSize());
		sizeBox.setEditor(new FixedBasicComboBoxEditor());
		sizeBox.setEditable(true);
		return sizeBox;
	}

	public Container createFontBox() {
		final JComboBox fontsBox = new JComboBoxWithBorder();
		fontsBox.setRenderer(new ComboBoxRendererWithTooltip(fontsBox));
		final Dimension preferredSize = fontsBox.getPreferredSize();
		fontsBox.setModel(fonts);
		return fontsBox;
	}

	@Override
	public void mapChanged(final MapChangeEvent event) {
		final Object property = event.getProperty();
		if (property.equals(MapStyle.MAP_STYLES)) {
			updateMapStyles(event.getMap());
			final Controller controller = Controller.getCurrentController();
			changeToolbar(controller.getSelection().getSelected());
			return;
		}
	}

	@Override
	public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
	}

	@Override
	public void onNodeInserted(final NodeModel parent, final NodeModel child, final int newIndex) {
	}

	@Override
	public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
	}

	@Override
	public void onPreNodeDelete(NodeDeletionEvent nodeDeletionEvent) {
	}

	@Override
	public void onPreNodeMoved(NodeMoveEvent nodeMoveEvent) {
	}

	@Override
	public void afterMapChange(final MapModel oldMap, final MapModel newMap) {
		updateMapStyles(newMap);
	}

	private void updateMapStyles(final MapModel newMap) {
		ignoreChangeEvent = true;
		styles.removeAllElements();
		if (newMap == null) {
			return;
		}
		final Collection<IStyle> styleObjects = MapStyleModel.getExtension(newMap).getStyles();
		for (final IStyle style : styleObjects) {
			styles.addElement(style);
		}
		ignoreChangeEvent = false;
	}

}
