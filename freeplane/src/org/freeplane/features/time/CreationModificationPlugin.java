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
package org.freeplane.features.time;

import java.awt.EventQueue;
import java.text.MessageFormat;
import org.freeplane.core.addins.NodeHookDescriptor;
import org.freeplane.core.addins.PersistentNodeHook;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.HistoryInformationModel;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.ITooltipProvider;
import org.freeplane.features.map.ModeController;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author foltin
 */
@NodeHookDescriptor(hookName = "accessories/plugins/CreationModificationPlugin.properties")
public class CreationModificationPlugin extends PersistentNodeHook implements INodeChangeListener, IExtension {
	private static final Integer CREATION_TOOLTIP = 14;
	private boolean nodeChangeListenerDisabled;
	private String tooltipFormat = null;

	/**
	 *
	 */
	public CreationModificationPlugin() {
		super();
		Controller.getCurrentModeController().getMapController().addNodeChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.extensions.NodeHook#invoke(freeplane.modes.MindMapNode)
	 */
	@Override
	public void add(final NodeModel node, final IExtension extension) {
		super.add(node, extension);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				Controller.getCurrentModeController().getMapController().removeNodeChangeListener(CreationModificationPlugin.this);
				try {
					setStyleRecursive(node);
				}
				finally {
					Controller.getCurrentModeController().getMapController().addNodeChangeListener(CreationModificationPlugin.this);
				}
			}
		});
	}

	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		return this;
	}

	public void nodeChanged(final NodeChangeEvent event) {
		if (nodeChangeListenerDisabled) {
			return;
		}
		if (!event.getProperty().equals(HistoryInformationModel.class)) {
			return;
		}
		final NodeModel node = event.getNode();
		if (!isActive(node)) {
			return;
		}
		nodeChangeListenerDisabled = true;
		try {
			setStyle(node);
		}
		finally {
			nodeChangeListenerDisabled = false;
		}
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
		setToolTip(node, CREATION_TOOLTIP, null);
		for (final NodeModel child : Controller.getCurrentModeController().getMapController().childrenUnfolded(node)) {
			removeToolTipRecursively(child);
		}
	}

	private void setStyle(final NodeModel node) {
		final Object[] messageArguments = { node.getHistoryInformation().getCreatedAt(),
		        node.getHistoryInformation().getLastModifiedAt() };
		if (tooltipFormat == null) {
			final StringBuilder sb = new StringBuilder();
			sb.append("<html>");
			sb.append(TextUtils.getText("plugins/TimeList.xml_Created"));
			sb.append(":  {0,date} {0,time}<br>");
			sb.append(TextUtils.getText("plugins/TimeList.xml_Modified"));
			sb.append(": {1,date} {1,time}</html>");
			tooltipFormat = sb.toString();
		}
		final MessageFormat formatter = new MessageFormat(tooltipFormat);
		final String message = formatter.format(messageArguments);
		setToolTip(node, CREATION_TOOLTIP, message);
	}

	/**
	 */
	private void setStyleRecursive(final NodeModel node) {
		setStyle(node);
		for (final NodeModel child : Controller.getCurrentModeController().getMapController().childrenUnfolded(node)) {
			setStyleRecursive(child);
		}
	}

	protected void setToolTip(final NodeModel node, final Integer key, final String value) {
		final ITooltipProvider tooltipProvider;
		if (value != null) {
			tooltipProvider = new ITooltipProvider() {
				public String getTooltip(ModeController modeController) {
					return value;
				}
			};
		}
		else {
			tooltipProvider = null;
		}
		final boolean nodeChangeListenerDisabled = this.nodeChangeListenerDisabled;
		this.nodeChangeListenerDisabled = true;
		try {
			(Controller.getCurrentModeController().getMapController()).setToolTip(node, key, tooltipProvider);
		}
		finally {
			this.nodeChangeListenerDisabled = nodeChangeListenerDisabled;
		}
	}
}
