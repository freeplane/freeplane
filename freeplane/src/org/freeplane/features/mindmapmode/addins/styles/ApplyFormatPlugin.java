/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JRootPane;
import javax.swing.JScrollPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.mindmapmode.MModeController;

@ActionLocationDescriptor(locations = { "/menu_bar/format/change" })
@SelectableAction(checkOnPopup = true)
public class ApplyFormatPlugin extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JScrollPane styleScrollPane;

	/**
	 */
	public ApplyFormatPlugin(final Controller controller) {
		super("ApplyFormatPlugin", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final ModeController modeController = getModeController();
		Controller controller = getController();
		Container rootPane = controller.getViewController().getContentPane();
		int stylePanelIndex = stylePanelIndex();
		if(stylePanelIndex != -1){
			rootPane.remove(stylePanelIndex);
			rootPane.validate();
			setSelected();
			return;
		}
		if(styleScrollPane == null){
			StyleEditorPanel panel = new StyleEditorPanel(modeController);
			panel.init(modeController);
			panel.setStyle(modeController, controller.getSelection().getSelected());
			 styleScrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		rootPane.add(styleScrollPane, BorderLayout.EAST);
		rootPane.validate();
		setSelected();
	}

	@Override
	public void setSelected() {
		super.setSelected(-1 != stylePanelIndex());
	}
	
	@Override
	public void afterMapChange(final Object newMap) {
		if(styleScrollPane != null){
			styleScrollPane.setVisible(newMap != null && getModeController() instanceof MModeController);
		}
		super.afterMapChange(newMap);
	}

	private int stylePanelIndex() {
		if(styleScrollPane == null){
			return -1;
		}
	Controller controller = getController();
	Container pane = controller.getViewController().getContentPane();
	for(int i = 0; i < pane.getComponentCount(); i++){
		if(styleScrollPane.equals(pane.getComponent(i))){
			return i;
		}
	}
	return -1;
	}
}
