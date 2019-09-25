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
package org.freeplane.view.swing.features;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.SysUtils;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.INodeView;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeDeletionEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.features.ui.INodeViewVisitor;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.view.swing.map.NodeView;

/**
 */
@NodeHookDescriptor(hookName = "accessories/plugins/BlinkingNodeHook.properties", onceForMap = false)
public class BlinkingNodeHook extends PersistentNodeHook {
	protected class TimerColorChanger extends TimerTask implements IExtension, IMapChangeListener,
	        IMapLifeCycleListener {
		final private NodeModel node;
		final private Timer timer;

		TimerColorChanger(final NodeModel node) {
			this.node = node;
			final MapController mapController = Controller.getCurrentModeController().getMapController();
			mapController.addUIMapChangeListener(this);
			mapController.addMapLifeCycleListener(this);
			timer = SysUtils.createTimer(getClass().getSimpleName());
			timer.schedule(this, 500, 500);
			BlinkingNodeHook.colors.clear();
			BlinkingNodeHook.colors.add(Color.BLUE);
			BlinkingNodeHook.colors.add(Color.RED);
			BlinkingNodeHook.colors.add(Color.MAGENTA);
			BlinkingNodeHook.colors.add(Color.CYAN);
		}

		public Iterable<NodeModel> getNodes() {
			return node != null ? node.allClones() : Collections.<NodeModel>emptyList();
		}

		public Timer getTimer() {
			return timer;
		}

		/** TimerTask method to enable the selection after a given time. */
		@Override
		public void run() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					final Iterable<NodeModel> nodes = getNodes();
					if (Controller.getCurrentModeController().isBlocked()) {
						return;
					}
					for(NodeModel node :nodes) {
						node.acceptViewVisitor(new INodeViewVisitor() {
							@Override
							public void visit(final INodeView nodeView) {
								if(! (nodeView instanceof NodeView)){
									return;
								}
								final Component container = ((NodeView)nodeView).getMainView();
								if (container == null || !container.isVisible()) {
									return;
								}
								final Color col = container.getForeground();
								int index = -1;
								if (col != null && BlinkingNodeHook.colors.contains(col)) {
									index = BlinkingNodeHook.colors.indexOf(col);
								}
								index++;
								if (index >= BlinkingNodeHook.colors.size()) {
									index = 0;
								}
								container.setForeground(BlinkingNodeHook.colors.get(index));
							}
						});
					}
				}
			});
		}

		@Override
		public void mapChanged(final MapChangeEvent event) {
		}

		@Override
		public void onNodeDeleted(NodeDeletionEvent nodeDeletionEvent) {
			if (Controller.getCurrentModeController().isUndoAction(node.getMap())
					|| !(node.equals(nodeDeletionEvent.node)
							|| node.isDescendantOf(nodeDeletionEvent.node))) {
				return;
			}
			final IActor actor = new IActor() {
				@Override
				public void act() {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							remove(node, node.getExtension(TimerColorChanger.class));
						}
					});
				}

				@Override
				public String getDescription() {
					return "BlinkingNodeHook.timer";
				}

				@Override
				public void undo() {
					node.addExtension(new TimerColorChanger(node));
				}
			};
			Controller.getCurrentModeController().execute(actor, node.getMap());
		}

		@Override
		public void onRemove(final MapModel map) {
			if (node.getMap().equals(map)) {
				timer.cancel();
			}
		}
	}

	static Vector<Color> colors = new Vector<Color>();

	public BlinkingNodeHook() {
		super();
	}

	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		return new TimerColorChanger(node);
	}

	@Override
	protected Class<TimerColorChanger> getExtensionClass() {
		return TimerColorChanger.class;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.extensions.MindMapHook#shutdownMapHook()
	 */
	@Override
	public void remove(final NodeModel node, final IExtension extension) {
		final TimerColorChanger timer = ((TimerColorChanger) extension);
		timer.getTimer().cancel();
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		mapController.removeMapChangeListener(timer);
		mapController.removeMapLifeCycleListener(timer);
		super.remove(node, extension);
	}
}
