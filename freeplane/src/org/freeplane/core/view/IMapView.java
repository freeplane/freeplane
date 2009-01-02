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
package org.freeplane.core.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import org.freeplane.core.map.MapModel;
import org.freeplane.core.map.NodeModel;
import org.freeplane.map.link.ArrowLinkModel;
import org.freeplane.view.swing.map.NodeView;

/**
 * @author Dimitry Polivaev
 * 02.01.2009
 */
public interface IMapView {
	public ArrowLinkModel detectCollision(final Point p);

	public MapModel getModel();

	public NodeView getNodeView(final NodeModel node);

	public NodeView getRoot();

	public NodeView getSelected();

	/**
	 * @return an ArrayList of MindMapNode objects. If both ancestor and
	 *         descandant node are selected, only the ancestor ist returned
	 */
	public ArrayList<NodeModel> getSelectedNodesSortedByY();

	/**
	 * @return
	 */
	public List<NodeView> getSelection();

	public float getZoom();

	/**
	 * Scroll the viewport of the map to the south-west, i.e. scroll the map
	 * itself to the north-east.
	 */
	public void scrollBy(final int x, final int y);

	public void scrollNodeToVisible(final NodeView node, final int extraWidth);

	/**
	 * Select the node, resulting in only that one being selected.
	 */
	public void selectAsTheOnlyOneSelected(final NodeView newSelected);

	public void setMoveCursor(final boolean isHand);

	public int getZoomed(int i);

	public Rectangle getInnerBounds();

	public void repaint();

	public void scrollNodeToVisible(NodeView selectedNodeView);

	public boolean isSelected(NodeView newlySelectedNodeView);

	public void toggleSelected(NodeView newlySelectedNodeView);

	public void centerNode(NodeView node);

	public Color getBackground();

	public Point getNodeContentLocation(NodeView nodeView);

	public void makeTheSelected(NodeView node);

	public void move(KeyEvent e);

	public void moveToRoot();

	public void resetShiftSelectionOrigin();

	public void selectBranch(NodeView newlySelectedNodeView, boolean extend);

	public boolean selectContinuous(NodeView newlySelectedNodeView);

	public void setSiblingMaxLevel(int nodeLevel);

	public List<NodeView> cloneSelection();

	public Component getComponent();

	public String getName();
}
