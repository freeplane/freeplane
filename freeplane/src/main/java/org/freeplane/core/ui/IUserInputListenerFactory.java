/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.core.ui;

import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelListener;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.menubuilders.generic.BuildPhaseListener;
import org.freeplane.core.ui.menubuilders.generic.BuilderDestroyerPair;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase;

public interface IUserInputListenerFactory {
	void addMouseWheelEventHandler(final IMouseWheelEventHandler handler);

	void addToolBar(final String name, final int position, final JComponent toolBar);

	IMouseListener getMapMouseListener();

	MouseWheelListener getMapMouseWheelListener();
	
	MouseWheelListener getNodeMouseWheelListener();

	JPopupMenu getMapPopup();

	public FreeplaneMenuBar getMenuBar();

	public Set<IMouseWheelEventHandler> getMouseWheelEventHandlers();

	DragGestureListener getNodeDragListener();

	DropTargetListener getNodeDropTargetListener();

	KeyListener getNodeKeyListener();

	IMouseListener getNodeMouseMotionListener();

	JPopupMenu getNodePopupMenu();

	JComponent getToolBar(String name);

	Iterable<JComponent> getToolBars(int position);

	public void removeMouseWheelEventHandler(final IMouseWheelEventHandler handler);

	void updateMapList();
	
	public void updateMenus(String menuStructureResource, Set<String> plugins);

	public void rebuildMenu(Entry entry);

	void addUiBuilder(Phase phase, String name, BuilderDestroyerPair builderDestroyerPair);
	
	public void addBuildPhaseListener(BuildPhaseListener listener);

	Entry getGenericMenuStructure();

	void rebuildMenus(String string);
}
