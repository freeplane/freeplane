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

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.url.NodeAndMapReference;


class OnlineDocumentationAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;
	private final NodeAndMapReference nodeAndMapReference;

	OnlineDocumentationAction( final String actionName, final String urlProperty) {
		super(actionName);
		final String urlString = ResourceController.getResourceController().getProperty(urlProperty);
		nodeAndMapReference = new NodeAndMapReference(urlString);
	}

	public void actionPerformed(final ActionEvent event) {
		try {
			final URL url = new URL(nodeAndMapReference.getMapReference());
			if(url == null)
				return;
			UITools.executeWhenNodeHasFocus( new Runnable() {
				
				@Override
				public void run() {
					try {
						if (nodeAndMapReference.hasFreeplaneFileExtension()) {
							Controller.getCurrentController().selectMode(MModeController.MODENAME);
							MMapController mapController = (MMapController)Controller.getCurrentModeController().getMapController();
							mapController.newDocumentationMap(url);
							if(nodeAndMapReference.hasNodeReference())
								mapController.select(nodeAndMapReference.getNodeReference());
						}
						else {
							Controller.getCurrentController().getViewController().openDocument(url);
						}
					}
					catch (final Exception e1) {
						LogUtils.severe(e1);
					}
				}
			});
        }
        catch (MalformedURLException ex) {
	        ex.printStackTrace();
        }
	}
}
