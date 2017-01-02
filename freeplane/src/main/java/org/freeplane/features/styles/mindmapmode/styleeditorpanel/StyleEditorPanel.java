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

import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.SeparatorProperty;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.map.AMapChangeListenerAdapter;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController;
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
	static final float FONT_SIZE = UITools.getUIFontSize(0.8);

	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	
	private boolean internalChange;
	private List<IPropertyControl> mControls;

	ControlGroup [] controlGroups;
	
	/**
	 * @throws HeadlessException
	 */
	public StyleEditorPanel(final ModeController modeController, final MUIFactory uiFactory,
	                        final boolean addStyleBox) throws HeadlessException {
		super();
		controlGroups = createControlGroups(modeController, uiFactory, addStyleBox);
		addHierarchyListener(new HierarchyListener() {
			
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
				new FontItalicControlGroup(),
				new NodeTextAlignmentControlGroup(),
				new NodeFontHyperLinkControlGroup(),
				new NextLineControlGroup(),
				new GroupSeparator("OptionPanel.separator.IconControls"),
				new IconSizeControlGroup(),
				new GroupSeparator("OptionPanel.separator.EdgeControls"),
				new EdgeWidthControlGroup(),
				new EdgeDashControlGroup(),
				new EdgeStyleControlGroup(),
				new EdgeColorControlGroup(),
				new NodeBackgroundColorControlGroup(),
				new NextLineControlGroup(),
				new GroupSeparator("OptionPanel.separator.CloudControls"),
				new CloudColorShapeControlGroup()
		};	}

	/**
	 * Creates all controls and adds them to the frame.
	 * @param modeController 
	 */
	private void init() {
		if(mControls != null)
			return;
		final String form = "right:max(20dlu;p), 2dlu, p, 1dlu,right:max(20dlu;p), 4dlu, 80dlu, 7dlu";
		final FormLayout rightLayout = new FormLayout(form, "");
		final DefaultFormBuilder formBuilder = new DefaultFormBuilder(rightLayout);
		formBuilder.border(Paddings.DLU2);
		new SeparatorProperty("OptionPanel.separator.NodeStyle").layout(formBuilder);
		final List<IPropertyControl> controls = new ArrayList<IPropertyControl>();
		
		for (ControlGroup controlGroup :controlGroups) {
			controlGroup.addControlGroup(controls, formBuilder);
		}
		mControls = controls;
		for (final IPropertyControl control : mControls) {
			control.layout(formBuilder);
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
				controlGroups[i].setStyle(node);
			}
			
		}
		finally {
			internalChange = false;
		}
	}

	private void addListeners() {
		final Controller controller = Controller.getCurrentController();
		final ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		mapController.addNodeSelectionListener(new INodeSelectionListener() {
			public void onSelect(final NodeModel node) {
				final IMapSelection selection = controller.getSelection();
				if (selection == null) {
					return;
				}
				if (selection.size() == 1) {
					setComponentsEnabled(true);
					setStyle(node);
				}
			}

			public void setComponentsEnabled(boolean enabled) {
				final Container panel = (Container) getComponent(0);
				for (int i = 0; i < panel.getComponentCount(); i++) {
					panel.getComponent(i).setEnabled(enabled);
				}
			}

			public void onDeselect(final NodeModel node) {
				setComponentsEnabled(false);
			}
		});
		mapController.addNodeChangeListener(new INodeChangeListener() {
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
		mapController.addMapChangeListener(new AMapChangeListenerAdapter() {

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
	}

}