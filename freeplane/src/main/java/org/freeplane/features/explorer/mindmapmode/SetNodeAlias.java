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
package org.freeplane.features.explorer.mindmapmode;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

class SetNodeAlias extends AFreeplaneAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private MMapExplorerController explorer;

	public SetNodeAlias(final MMapExplorerController explorer) {
		super("SetNodeAliasAction");
		this.explorer = explorer;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
		final String alias = explorer.getAlias(node);
		final String title = TextUtils.getText(getTextKey());
		final JLabel aliasLabel = TranslatedElementFactory.createLabel("node_alias");
		final JTextField aliasInput = new JTextField(alias, 40);

		JCheckBox globalNodeCheckBox = TranslatedElementFactory.createCheckBox("globally_accessible");
		globalNodeCheckBox.setSelected(explorer.isGlobal(node));
		Box components = Box.createVerticalBox();
		components.add(aliasLabel);
		components.add(aliasInput);
		components.add(globalNodeCheckBox);
		if(JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner(), components, title, JOptionPane.OK_CANCEL_OPTION,
		 JOptionPane.PLAIN_MESSAGE)){
			explorer.setAlias(node, aliasInput.getText());
			explorer.makeGlobal(node, globalNodeCheckBox.isSelected());
		}
	}

}
