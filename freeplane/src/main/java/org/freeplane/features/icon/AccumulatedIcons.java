/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.features.icon;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.icon.HierarchicalIcons.Mode;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 * Dec 10, 2011
 */
public class AccumulatedIcons  implements IExtension{
	private final TreeSet<UIIcon> childIcons ;
	private final Collection<? extends UIIcon> ownIcons;

	AccumulatedIcons(Collection<? extends UIIcon> ownIcons) {
		this.ownIcons = ownIcons;
		this.childIcons = new TreeSet<UIIcon>();
    } 

	public Collection<UIIcon> getAccumulatedIcons() {
	    return childIcons;
    }

	/**
	 */
	private void addAccumulatedIconsToTreeSet(final NodeModel child) {
		final IconController iconController = IconController.getController();
		childIcons.addAll(iconController.getIcons(child));
		final AccumulatedIcons icons = (AccumulatedIcons) child.getExtension(AccumulatedIcons.class);
		if (icons == null) {
			return;
		}
		childIcons.addAll((Collection<UIIcon>) icons.childIcons);
	}

	static public boolean setStyleCheckForChange(final NodeModel node, Mode mode) {
		final Collection<MindIcon> ownIcons = IconController.getController().getIcons(node);
		final AccumulatedIcons iconSet = new AccumulatedIcons(ownIcons);
		boolean first = true;
		for (final NodeModel child : Controller.getCurrentModeController().getMapController().childrenUnfolded(node)) {
			if(first || mode.equals(Mode.OR)){
				iconSet.addAccumulatedIconsToTreeSet(child);
			}
			else{
				@SuppressWarnings("unchecked")
                final AccumulatedIcons iconSet2 = new AccumulatedIcons(Collections.EMPTY_SET);
				iconSet2.addAccumulatedIconsToTreeSet(child);
				iconSet.childIcons.retainAll((Collection<UIIcon>) iconSet2.childIcons);
				if(iconSet.ownIcons.isEmpty() && iconSet.childIcons.isEmpty())
					break;
			}
			first = false;
		}
		iconSet.childIcons.removeAll(ownIcons);
		
		final AccumulatedIcons oldSet;
		if (! (iconSet.ownIcons.isEmpty() && iconSet.childIcons.isEmpty())) {
			oldSet = (AccumulatedIcons)node.putExtension(iconSet);
		}
		else {
			oldSet = node.removeExtension(AccumulatedIcons.class);
		}
		if(iconSet.equals(oldSet) || iconSet.ownIcons.isEmpty() && iconSet.childIcons.isEmpty() && oldSet == null)
			return false;
		Controller.getCurrentModeController().getMapController().delayedNodeRefresh(node, HierarchicalIcons.ICONS, null, null);
		return true;
	}
	@Override
    public int hashCode() {
	    return childIcons.hashCode() + ownIcons.hashCode();
    }

	@Override
    public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj instanceof AccumulatedIcons){
			AccumulatedIcons ai = (AccumulatedIcons) obj;
			return ownIcons.equals(ai.ownIcons) && childIcons.equals(ai.childIcons);
		}
		return false;
    } 
	
	
}
