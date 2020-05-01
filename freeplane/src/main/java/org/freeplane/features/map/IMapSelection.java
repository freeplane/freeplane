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
package org.freeplane.features.map;

import java.util.List;
import java.util.Set;

import org.freeplane.features.filter.Filter;


/**
 * @author Dimitry Polivaev
 * 04.01.2009
 */
public interface IMapSelection {

	public enum NodePosition {
		WEST, CENTER, EAST;
	}
	
	public void centerNode(final NodeModel node);
	
	public void centerNodeSlowly(final NodeModel node);

	public void moveNodeTo(final NodeModel node, NodePosition position);
	
	public void slowlyMoveNodeTo(final NodeModel node, NodePosition position);

	public NodeModel getSelected();

	public Set<NodeModel> getSelection();
	
	public List<String> getOrderedSelectionIds();

	public List<NodeModel> getOrderedSelection();

	public List<NodeModel> getSortedSelection(boolean differentSubtrees);

	public boolean isSelected(final NodeModel node);

	public void keepNodePosition(final NodeModel node, float horizontalPoint, float verticalPoint);
	
	public void scrollNodeTreeToVisible(final NodeModel  node);

	public void makeTheSelected(final NodeModel node);

	public void scrollNodeToVisible(NodeModel selected);

	public void selectAsTheOnlyOneSelected(final NodeModel node);

	public void selectBranch(final NodeModel node, final boolean extend);

	public void selectContinuous(final NodeModel node);

	public void selectRoot();

	public void setSiblingMaxLevel(int nodeLevel);

	public int size();

	public void toggleSelected(final NodeModel node);
	
	public void replaceSelection(NodeModel[] nodes);
	
	Filter getFilter();
	
	void setFilter(Filter filter);

    default MapModel getMap() {
        return getSelected().getMap();
    }
	
}
