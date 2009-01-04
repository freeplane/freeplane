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
package org.freeplane.features.common.link;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.JMenuItem;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.FreeplaneAction;

class FollowLinkAction extends FreeplaneAction implements PopupMenuListener {
	public FollowLinkAction() {
		super("follow_link");
	}

	public void actionPerformed(final ActionEvent e) {
		for (final Iterator iterator = getModeController().getMapController().getSelectedNodes()
		    .iterator(); iterator.hasNext();) {
			final NodeModel selNode = (NodeModel) iterator.next();
			if (NodeLinks.getLink(selNode) != null) {
				getModeController().getMapController().loadURL(NodeLinks.getLink(selNode));
			}
		}
	}

	private boolean isLinkEnabled() {
		for (final Iterator iterator = getModeController().getMapController().getSelectedNodes()
		    .iterator(); iterator.hasNext();) {
			final NodeModel selNode = (NodeModel) iterator.next();
			if (NodeLinks.getLink(selNode) != null) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.PopupMenuListener#popupMenuCanceled(javax.swing
	 * .event.PopupMenuEvent)
	 */
	public void popupMenuCanceled(final PopupMenuEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeInvisible(
	 * javax.swing.event.PopupMenuEvent)
	 */
	public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeVisible(javax
	 * .swing.event.PopupMenuEvent)
	 */
	public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
		final JMenuItem item = (JMenuItem) e.getSource();
		item.setEnabled(isLinkEnabled());
	}
}
