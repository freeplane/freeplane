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
package org.freeplane.features.link.mindmapmode.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import javax.swing.JPanel;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.SeparatorProperty;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.link.ConnectorModel;
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

public class ConnectorEditorPanel extends JPanel {

	static final float FONT_SIZE = UITools.getUIFontSize(0.8);

	/**
	*
	*/
	private static final long serialVersionUID = 1L;

	ControlGroup [] controlGroups;

    private ConnectorModel connector;

	/**
	 * @throws HeadlessException
	 */
	public ConnectorEditorPanel(final ModeController modeController, ConnectorModel connector) throws HeadlessException {
		super();
        this.connector = connector;
		controlGroups = createControlGroups(modeController);
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

	private ControlGroup[] createControlGroups(ModeController modeController) {
		return new ControlGroup[]{
		        new ConnectorColorControlGroup(),
		        new OpacityControlGroup(),
		        new ConnectorArrowsControlGroup(),
		        new ConnectorShapeControlGroup(),
		        new ConnectorDashControlGroup(),
		        new ConnectorWidthControlGroup(),
				new FontNameControlGroup(),
				new FontSizeControlGroup(),
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
		for (ControlGroup controlGroup :controlGroups) {
			controlGroup.addControlGroup(formBuilder);
			controlGroup.updateValue(connector);
		}
		add(formBuilder.getPanel(), BorderLayout.CENTER);
	}
}