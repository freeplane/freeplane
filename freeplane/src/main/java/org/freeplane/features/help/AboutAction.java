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
package org.freeplane.features.help;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;


class AboutAction extends AFreeplaneAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	AboutAction() {
		super("AboutAction");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		Box box = Box.createVerticalBox();
		String about = TextUtils.getText("about_text") + " " + FreeplaneVersion.getVersion();
		addUri(box, "homepage_url", about);
		addUri(box, "copyright_url", TextUtils.getText("copyright"));
		final String revision = FreeplaneVersion.getVersion().getRevision();
		if(! revision.isEmpty())
			addMessage(box, revision);
		addFormattedMessage(box, "java_version", Compat.JAVA_VERSION);
		addFormattedMessage(box, "main_resource_directory", ResourceController.getResourceController().getResourceBaseDir());
		addUri(box, "license_url", TextUtils.getText("license"));
		addMessage(box, TextUtils.getText("license_text"));

		JOptionPane.showMessageDialog(UITools.getCurrentRootComponent(), box, TextUtils
		    .getText("AboutAction.text"), JOptionPane.INFORMATION_MESSAGE);
	}

	private void addFormattedMessage(Box box, String format, String parameter) {
		addMessage(box,TextUtils.format(format, parameter));
	}

	private void addMessage(Box box, String text) {
		final JComponent textComponent;
		if(HtmlUtils.isHtmlNode(text)) {
			textComponent = new JLabel(text);
		}
		else {
			final JTextField textField = new JTextField(text);
			textField.setEditable(false);
			textField.setBorder(BorderFactory.createEmptyBorder());
			textComponent = textField;
		}
		textComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
		box.add(textComponent);
	}

	private void addUri(Box box, String uriProperty, String message) {
		try {
			URI uri;
			uri = new URI( ResourceController.getResourceController().getProperty(uriProperty));
			JButton uriButton = UITools.createHtmlLinkStyleButton(uri, message);
			uriButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			uriButton.setHorizontalAlignment(SwingConstants.LEADING);
			box.add(uriButton);
		} catch (URISyntaxException e1) {
		}
	}
}
