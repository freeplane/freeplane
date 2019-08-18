/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Stefan Ott in 2011.
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
package org.freeplane.plugin.latex;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * 
 * @author Stefan Ott
 *
 *This class is called when a (legacy!) LaTeX formula is inserted into
 * (added to) a node
 * @see http://freeplane.sourceforge.net/wiki/index.php/LaTeX_in_Freeplane
 */
@EnabledAction(checkOnNodeChange = true)
public class InsertLatexAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private final LatexNodeHook nodeHook;

	public InsertLatexAction(final LatexNodeHook nodeHook) {
		super("LatexInsertLatexAction");
		this.nodeHook = nodeHook;
	}

	public void actionPerformed(final ActionEvent arg0) {
		Box box = Box.createVerticalBox();
		final String about1 = TextUtils.getText("LatexInsertLatexAction.msg1");
		box.add(new JLabel(about1));
		addUriWithoutTitle(box, "LaTeX_in_freeplane_url");
		final String about2 = TextUtils.getText("LatexInsertLatexAction.msg2");
		box.add(new JLabel(about2));
		
		JOptionPane.showMessageDialog(UITools.getCurrentRootComponent(), box, TextUtils
		    .getText("LatexInsertLatexAction.text"), JOptionPane.INFORMATION_MESSAGE);
	}

	private void addUriWithoutTitle(Box box, String uriProperty) {
		try {
			final String urlText = ResourceController.getResourceController().getProperty(uriProperty); 
			URI uri = new URI(urlText);
			JButton uriButton = UITools.createHtmlLinkStyleButton(uri, urlText);
			uriButton.setHorizontalAlignment(SwingConstants.LEADING);
			box.add(uriButton);
		} catch (URISyntaxException e1) {
		}
	}

	@Override
	public void setEnabled() {
		final NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
		setEnabled(node != null && (LatexExtension) node.getExtension(LatexExtension.class) == null);
	}
}
