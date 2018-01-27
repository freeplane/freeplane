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

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.ITooltipProvider;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class CreationModificationDatePresenter {
	private static final String SHOW_CREATION_MODIFICATION_IN_TOOLTIP_PROPERTY = "show_creation_modification_in_tooltip";
	private static final String SHOW_CREATION_MODIFICATION_IN_STATUS_PROPERTY = "show_creation_modification_in_status";
	private static final Integer CREATION_TOOLTIP = 14;
	public static final String HOOK_NAME = "accessories/plugins/CreationModificationPlugin.properties";
	private static MessageFormat formatter;

	public CreationModificationDatePresenter() {
		super();
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addToolTipProvider(CREATION_TOOLTIP, new ITooltipProvider() {
			@Override
			public String getTooltip(ModeController modeController, NodeModel node, Component view) {
				if (!(node.getHistoryInformation().isSet() && ResourceController.getResourceController()
				    .getBooleanProperty(SHOW_CREATION_MODIFICATION_IN_TOOLTIP_PROPERTY)))
					return null;
				initTooltipFormat();
				final Object[] messageArguments = getMessageArguments(node);
				final String message = formatter.format(messageArguments);
				return message;
			}
		});
		Controller.getCurrentController().getViewController().addStatusInfo(HOOK_NAME, "");
		modeController.getMapController().addNodeSelectionListener(new INodeSelectionListener() {
			@Override
			public void onSelect(NodeModel node) {
				if (!(node.getHistoryInformation().isSet() && ResourceController.getResourceController()
				    .getBooleanProperty(SHOW_CREATION_MODIFICATION_IN_STATUS_PROPERTY)))
					return;
				showStatusInfo(node);
			}

			@Override
			public void onDeselect(NodeModel node) {
				hideStatusInfo();
			}
		});
		ResourceController.getResourceController().addPropertyChangeListener(new IFreeplanePropertyListener() {
			@Override
			public void propertyChanged(String propertyName, String newValue, String oldValue) {
				if (propertyName.equals(SHOW_CREATION_MODIFICATION_IN_STATUS_PROPERTY))
					if (Boolean.parseBoolean(newValue)) {
						final IMapSelection selection = Controller.getCurrentController().getSelection();
						if (selection != null) {
							final NodeModel selected = selection.getSelected();
							if (selected != null)
								showStatusInfo(selected);
						}
					}
					else
						hideStatusInfo();
			}
		});
	}

	private Object[] getMessageArguments(NodeModel node) {
		final Object[] messageArguments = { node.getHistoryInformation().getCreatedAt(),
		        node.getHistoryInformation().getLastModifiedAt() };
		return messageArguments;
	}

	private void initTooltipFormat() {
		if (formatter == null) {
			final StringBuilder sb = new StringBuilder();
			sb.append("<html>");
			sb.append(TextUtils.getText("plugins/TimeList.xml_Created"));
			sb.append(":  {0,date} {0,time}<br>");
			sb.append(TextUtils.getText("plugins/TimeList.xml_Modified"));
			sb.append(": {1,date} {1,time}</html>");
			String tooltipFormat = sb.toString();
			formatter = new MessageFormat(tooltipFormat);
		}
	}

	private void showStatusInfo(NodeModel node) {
		initTooltipFormat();
		final Object[] messageArguments = getMessageArguments(node);
		final String message = formatter.format(messageArguments);
		Controller.getCurrentController().getViewController().addStatusInfo(HOOK_NAME, message);
	}

	private void hideStatusInfo() {
		Controller.getCurrentController().getViewController().addStatusInfo(HOOK_NAME, "");
	}
}
