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

import java.awt.Component;
import java.text.MessageFormat;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.ITooltipProvider;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author foltin
 */
@NodeHookDescriptor(hookName = "accessories/plugins/CreationModificationPlugin.properties", onceForMap = true)
public class CreationModificationPlugin extends PersistentNodeHook implements IExtension {
	private static final Integer CREATION_TOOLTIP = 14;
	private String tooltipFormat = null;

	public CreationModificationPlugin() {
	    super();
	    Controller.getCurrentModeController().addToolTipProvider(CREATION_TOOLTIP, new ITooltipProvider() {
			public String getTooltip(ModeController modeController, NodeModel node, Component view) {
				if (! isActive(node))
					return null;
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
				return message;
			}
		});
    }

	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		return this;
	}

}
