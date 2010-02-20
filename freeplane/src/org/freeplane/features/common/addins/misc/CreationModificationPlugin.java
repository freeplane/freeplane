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

import java.awt.EventQueue;
import java.text.MessageFormat;
import java.util.Iterator;

import org.freeplane.core.addins.NodeHookDescriptor;
import org.freeplane.core.addins.PersistentNodeHook;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.modecontroller.INodeChangeListener;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.modecontroller.NodeChangeEvent;
import org.freeplane.core.model.HistoryInformationModel;
import org.freeplane.core.model.ITooltipProvider;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author foltin
 */
@NodeHookDescriptor(hookName = "accessories/plugins/CreationModificationPlugin.properties")
@ActionLocationDescriptor(locations = { "/menu_bar/extras/first/nodes/change" })
public class CreationModificationPlugin extends PersistentNodeHook implements INodeChangeListener, IExtension {
	private boolean nodeChangeListenerDisabled;
	private String tooltipFormat = "<html>Created:  {0,date} {0,time}<br>Modified: {1,date} {1,time}</html>";

	/**
	 *
	 */
	public CreationModificationPlugin(final ModeController modeControler) {
		super(modeControler);
		modeControler.getMapController().addNodeChangeListener(this);
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
				getModeController().getMapController().removeNodeChangeListener(CreationModificationPlugin.this);
				try {
					setStyleRecursive(node);
				}
				finally {
					getModeController().getMapController().addNodeChangeListener(CreationModificationPlugin.this);
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
		if (! event.getProperty().equals(HistoryInformationModel.class)) {
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
		setToolTip(node, getHookName(), null);
		for (final Iterator<NodeModel> i = getModeController().getMapController().childrenUnfolded(node); i.hasNext();) {
			final NodeModel child = i.next();
			removeToolTipRecursively(child);
		}
	}

	private void setStyle(final NodeModel node) {
		final Object[] messageArguments = { node.getHistoryInformation().getCreatedAt(),
		        node.getHistoryInformation().getLastModifiedAt() };
		if (tooltipFormat == null) {
			tooltipFormat = ResourceBundles.getText("CreationModificationPlugin.tooltip_format");
		}
		final MessageFormat formatter = new MessageFormat(tooltipFormat);
		final String message = formatter.format(messageArguments);
		setToolTip(node, getHookName(), message);
	}

	/**
	 */
	private void setStyleRecursive(final NodeModel node) {
		setStyle(node);
		for (final Iterator<NodeModel> i = getModeController().getMapController().childrenFolded(node); i.hasNext();) {
			final NodeModel child = i.next();
			setStyleRecursive(child);
		}
	}

	protected void setToolTip(final NodeModel node, final String key, final String value) {
		final ITooltipProvider tooltipProvider;
		if(value != null){
		tooltipProvider = new ITooltipProvider() {
			public String getTooltip() {
				return value;
			}
		};
		}
		else{
			tooltipProvider = null;
		}
		final boolean nodeChangeListenerDisabled = this.nodeChangeListenerDisabled;
		this.nodeChangeListenerDisabled = true;
		try{
		(getModeController().getMapController()).setToolTip(node, key, tooltipProvider);
		}
		finally{
			this.nodeChangeListenerDisabled = nodeChangeListenerDisabled;
		}
	}
}
