/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file author is Christian Foltin
 *  It is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.features.common.addins.misc;

import java.text.MessageFormat;
import java.util.Iterator;

import org.freeplane.core.addins.NodeHookDescriptor;
import org.freeplane.core.addins.PersistentNodeHook;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.modecontroller.INodeChangeListener;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.modecontroller.NodeChangeEvent;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ActionDescriptor;

/**
 * @author foltin
 */
@NodeHookDescriptor(hookName = "accessories/plugins/CreationModificationPlugin.properties")
@ActionDescriptor(locations = { "/menu_bar/extras/first/nodes/change" }, //
name = "accessories/plugins/CreationModificationPlugin.properties_name", //
iconPath = "/images/kword.png", //
tooltip = "accessories/plugins/CreationModificationPlugin.properties_documentation")
public class CreationModificationPlugin extends PersistentNodeHook implements INodeChangeListener {
	private String tooltipFormat = "<html>Created:  {0,date} {0,time}<br>Modified: {1,date} {1,time}</html>";

	/**
	 *
	 */
	public CreationModificationPlugin(final ModeController modeControler) {
		super(modeControler);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.extensions.NodeHook#invoke(freeplane.modes.MindMapNode)
	 */
	@Override
	public void add(final NodeModel node, final IExtension extension) {
		super.add(node, extension);
		setStyleRecursive(node);
	}

	public void nodeChanged(final NodeChangeEvent event) {
		final NodeModel node = event.getNode();
		if (!isActive(node)) {
			return;
		}
		setStyle(node);
	}

	@Override
	public void remove(final NodeModel node, final IExtension extension) {
		removeToolTipRecursively(node);
		super.remove(node, extension);
	}

	/**
	 *
	 */
	private void removeToolTipRecursively(final NodeModel node) {
		setToolTip(node, getHookName(), null);
		for (final Iterator i = node.getModeController().getMapController().childrenUnfolded(node); i.hasNext();) {
			final NodeModel child = (NodeModel) i.next();
			removeToolTipRecursively(child);
		}
	}

	private void setStyle(final NodeModel node) {
		final Object[] messageArguments = { node.getHistoryInformation().getCreatedAt(),
		        node.getHistoryInformation().getLastModifiedAt() };
		if (tooltipFormat == null) {
			// TODO
			tooltipFormat = ResourceController.getText("CreationModificationPlugin.tooltip_format");
		}
		final MessageFormat formatter = new MessageFormat(tooltipFormat);
		final String message = formatter.format(messageArguments);
		setToolTip(node, getHookName(), message);
	}

	/**
	 */
	private void setStyleRecursive(final NodeModel node) {
		setStyle(node);
		for (final Iterator i = node.getModeController().getMapController().childrenFolded(node); i.hasNext();) {
			final NodeModel child = (NodeModel) i.next();
			setStyleRecursive(child);
		}
	}

	protected void setToolTip(final NodeModel node, final String key, final String value) {
		(getModeController().getMapController()).setToolTip(node, key, value);
	}
}
