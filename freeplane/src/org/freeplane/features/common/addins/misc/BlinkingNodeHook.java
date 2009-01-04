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

import java.awt.Color;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.freeplane.core.addins.NodeHookDescriptor;
import org.freeplane.core.addins.PersistentNodeHook;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IXMLElement;
import org.freeplane.core.modecontroller.INodeViewVisitor;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.ActionDescriptor;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.NodeView;

/**
 */
@NodeHookDescriptor(hookName = "accessories/plugins/BlinkingNodeHook.properties", onceForMap = false)
@ActionDescriptor(name = "accessories/plugins/BlinkingNodeHook.properties_name", //
iconPath = "accessories/plugins/icons/xeyes.png", //
tooltip = "accessories/plugins/BlinkingNodeHook.properties_documentation", //
locations = { "/menu_bar/format/nodes" })
public class BlinkingNodeHook extends PersistentNodeHook {
	protected class TimerColorChanger extends TimerTask implements IExtension {
		final private NodeModel node;
		final private Timer timer;

		TimerColorChanger(final NodeModel node) {
			this.node = node;
			timer = new Timer();
			timer.schedule(this, 500, 500);
			BlinkingNodeHook.colors.clear();
			BlinkingNodeHook.colors.add(Color.BLUE);
			BlinkingNodeHook.colors.add(Color.RED);
			BlinkingNodeHook.colors.add(Color.MAGENTA);
			BlinkingNodeHook.colors.add(Color.CYAN);
		}

		public NodeModel getNode() {
			return node;
		}

		public Timer getTimer() {
			return timer;
		}

		/** TimerTask method to enable the selection after a given time. */
		@Override
		public void run() {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (getNode() == null || getModeController().isBlocked()) {
						return;
					}
					getNode().acceptViewVisitor(new INodeViewVisitor() {
						public void visit(final NodeView view) {
							if (!view.isVisible()) {
								return;
							}
							final MainView mainView = view.getMainView();
							final Color col = mainView.getForeground();
							int index = -1;
							if (col != null && BlinkingNodeHook.colors.contains(col)) {
								index = BlinkingNodeHook.colors.indexOf(col);
							}
							index++;
							if (index >= BlinkingNodeHook.colors.size()) {
								index = 0;
							}
							mainView.setForeground((Color) BlinkingNodeHook.colors.get(index));
						}
					});
				}
			});
		}
	}

	static Vector colors = new Vector();

	/**
	 */
	public BlinkingNodeHook(final ModeController modeController) {
		super(modeController);
	}

	@Override
	protected IExtension createExtension(final NodeModel node, final IXMLElement element) {
		return new TimerColorChanger(node);
	}

	@Override
	protected Class getExtensionClass() {
		return TimerColorChanger.class;
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.extensions.MindMapHook#shutdownMapHook()
	 */
	@Override
	public void remove(final NodeModel node, final IExtension extension) {
		((TimerColorChanger) extension).getTimer().cancel();
		super.remove(node, extension);
	}
}
