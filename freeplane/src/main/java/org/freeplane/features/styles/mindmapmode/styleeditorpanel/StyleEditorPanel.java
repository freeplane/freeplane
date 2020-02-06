/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.SeparatorProperty;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.mindmapmode.MUIFactory;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Paddings;
import com.jgoodies.forms.layout.FormLayout;

public class StyleEditorPanel extends JPanel {
	private final class PanelEnabler implements IFreeplanePropertyListener, IMapSelectionListener {
		private final Controller controller;
		private final ModeController modeController;
		boolean canEdit = true;

		private PanelEnabler(Controller controller, ModeController modeController) {
			this.controller = controller;
			this.modeController = modeController;
		}

		@Override
		public void propertyChanged(String propertyName, String newValue, String oldValue) {
			if(propertyName.equals(ModeController.VIEW_MODE_PROPERTY))
				updatePanel();
		}
		
		@Override
		public void afterMapChange(final MapModel oldMap, final MapModel newMap) {
			if (modeController.equals(Controller.getCurrentModeController())) {
				updatePanel();
			}
		}

		private void updatePanel() {
			boolean canEditNow = modeController.canEdit(controller.getMap());
			if(canEdit != canEditNow) {
				canEdit = canEditNow;
				setComponentsEnabled(canEdit);
			}
		}
	}

	static final float FONT_SIZE = UITools.getUIFontSize(0.8);

	/**
	*
	*/
	private static final long serialVersionUID = 1L;

	private boolean internalChange;
	ControlGroup [] controlGroups;

	private PanelEnabler panelEnabler;

	/**
	 * @throws HeadlessException
	 */
	public StyleEditorPanel(final ModeController modeController, final MUIFactory uiFactory,
	                        final boolean addStyleBox) throws HeadlessException {
		super();
		controlGroups = createControlGroups(modeController, uiFactory, addStyleBox);
		addHierarchyListener(new HierarchyListener() {

			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				if(isDisplayable()){
					removeHierarchyListener(this);
					init();
				}
			}
		});
	}

	private ControlGroup[] createControlGroups(ModeController modeController, MUIFactory uiFactory, boolean addStyleBox) {
		return new ControlGroup[]{
				new StyleControlGroup(addStyleBox, uiFactory, modeController),
				new GroupSeparator("OptionPanel.separator.NodeColors"),
				new NodeColorControlGroup(),
				new NodeBackgroundColorControlGroup(),
				new GroupSeparator("OptionPanel.separator.NodeText"),
				new FormatControlGroup(),
				new NodeNumberingControlGroup(),
				new GroupSeparator("OptionPanel.separator.NodeShape"),
				new NodeShapeControlGroup(),
				new MinNodeWidthControlGroup(),
				new MaxNodeWidthControlGroup(),
				new ChildDistanceControlGroup(),
				new GroupSeparator("OptionPanel.separator.NodeBorder"),
				new BorderWidthAndBorderWidthMatchesEdgeControlGroup(),
				new BorderDashAndDashMatchesEdgeControlGroup(),
				new BorderColorAndColorMatchesEdgeControlGroup(),
				new NextLineControlGroup(),
				new GroupSeparator("OptionPanel.separator.NodeFont"),
				new FontNameControlGroup(),
				new FontSizeControlGroup(),
				new FontBoldControlGroup(),
				new FontStrikeThroughControlGroup(),
				new FontItalicControlGroup(),
				new NodeHorizontalTextAlignmentControlGroup(),
				new NodeFontHyperLinkControlGroup(),
				new NextLineControlGroup(),
				new GroupSeparator("OptionPanel.separator.IconControls"),
				new IconSizeControlGroup(),
				new GroupSeparator("OptionPanel.separator.EdgeControls"),
				new EdgeWidthControlGroup(),
				new EdgeDashControlGroup(),
				new EdgeStyleControlGroup(),
				new EdgeColorControlGroup(),
				new NextLineControlGroup(),
				new GroupSeparator("OptionPanel.separator.CloudControls"),
				new CloudColorShapeControlGroup()
		};	}

	/**
	 * Creates all controls and adds them to the frame.
	 * @param modeController
	 */
	private void init() {
		final String form = "right:max(20dlu;p), 2dlu, p, 1dlu,right:max(20dlu;p), 4dlu, 80dlu, 7dlu";
		final FormLayout rightLayout = new FormLayout(form, "");
		final DefaultFormBuilder formBuilder = new DefaultFormBuilder(rightLayout);
		formBuilder.border(Paddings.DLU2);
		new SeparatorProperty("OptionPanel.separator.NodeStyle").layout(formBuilder);

		for (ControlGroup controlGroup :controlGroups) {
			controlGroup.addControlGroup(formBuilder);
		}
		add(formBuilder.getPanel(), BorderLayout.CENTER);
		addListeners();
		setFont(this, FONT_SIZE);
	}

	private void setFont(Container c, float size) {
		c.setFont(c.getFont().deriveFont(size));
		for(int i = 0; i < c.getComponentCount(); i++){
			setFont((Container) c.getComponent(i), size);
		}
    }

	public void setStyle( final NodeModel node) {
		if (internalChange) {
			return;
		}
		internalChange = true;
		try {
			for (int i=0; i<controlGroups.length; i++) {
				controlGroups[i].setStyle(node, panelEnabler != null && panelEnabler.canEdit);
			}

		}
		finally {
			internalChange = false;
		}
	}

	private void setComponentsEnabled(boolean enabled) {
		final Container panel = (Container) getComponent(0);
		for (int i = 0; i < panel.getComponentCount(); i++) {
			panel.getComponent(i).setEnabled(enabled);
		}
	}
	
	private void addListeners() {
		final Controller controller = Controller.getCurrentController();
		final ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		mapController.addNodeSelectionListener(new INodeSelectionListener() {
			@Override
			public void onSelect(final NodeModel node) {
				final IMapSelection selection = controller.getSelection();
				if (selection == null) {
					return;
				}
				if (selection.size() == 1) {
					if(modeController.canEdit(selection.getSelected().getMap()))
						setComponentsEnabled(true);
					setStyle(node);
				}
			}

			@Override
			public void onDeselect(final NodeModel node) {
				setComponentsEnabled(false);
			}
		});
		mapController.addUINodeChangeListener(new INodeChangeListener() {
			@Override
			public void nodeChanged(final NodeChangeEvent event) {
				final IMapSelection selection = controller.getSelection();
				if (selection == null) {
					return;
				}
				final NodeModel node = event.getNode();
				if (selection.getSelected().equals(node)) {
					setStyle(node);
				}
			}
		});
		mapController.addUIMapChangeListener(new IMapChangeListener() {

			@Override
            public void mapChanged(MapChangeEvent event) {
				if(! MapStyle.MAP_STYLES.equals(event.getProperty()))
					return;
				final IMapSelection selection = controller.getSelection();
				if (selection == null) {
					return;
				}
				final NodeModel node = selection.getSelected();
				setStyle(node);
            }

		});
		
		panelEnabler = new PanelEnabler(controller, modeController);
		controller.getMapViewManager().addMapSelectionListener(panelEnabler);
		ResourceController.getResourceController().addPropertyChangeListener(panelEnabler);
	}

}