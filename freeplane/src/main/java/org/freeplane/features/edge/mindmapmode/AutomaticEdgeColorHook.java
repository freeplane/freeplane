/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 dimitry
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
package org.freeplane.features.edge.mindmapmode;

import java.awt.Color;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.features.edge.EdgeController;
import org.freeplane.features.edge.EdgeModel;
import org.freeplane.features.map.AMapChangeListenerAdapter;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.features.styles.LogicalStyleModel;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Nov 28, 2010
 */

@NodeHookDescriptor(hookName = "AutomaticEdgeColor")
public class AutomaticEdgeColorHook extends PersistentNodeHook implements IExtension{
	private ModeController modeController;

	private class Listener extends AMapChangeListenerAdapter{
		@Override
	    public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
			if(!isActive(child) || modeController.isUndoAction()){
				return;
			}
			if(MapStyleModel.FLOATING_STYLE.equals(LogicalStyleModel.getStyle(child)))
				return;
			if(parent.isRoot()){
				final EdgeModel edgeModel = EdgeModel.createEdgeModel(child);
				if(null == edgeModel.getColor()){
					final MEdgeController controller = (MEdgeController) EdgeController.getController();
					final AutomaticEdgeColor model = (AutomaticEdgeColor) getMapHook();
					controller.setColor(child, model.nextColor());
				}
			}
			else{
				final MEdgeController controller = (MEdgeController) EdgeController.getController();
				controller.setColor(child, null);
				final boolean edgeStylesEquals = controller.getColor(child).equals(controller.getColor(parent));
				if(! edgeStylesEquals){
					OptionalDontShowMeAgainDialog.show("edge_is_formatted_by_style", "confirmation",
					    "ignore_edge_format_by_style", OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_SHOWN);
				}
			}
	    }

        @Override
        public void onNodeMoved(NodeModel oldParent, int oldIndex, NodeModel newParent, NodeModel child, int newIndex) {
            onNodeInserted(newParent, child, newIndex);
        }
	}

	public AutomaticEdgeColorHook() {
	    super();
		final Listener listener = new Listener();
		modeController = Controller.getCurrentModeController();
		modeController.addExtension(AutomaticEdgeColorHook.class, this);
		final MapController mapController = modeController.getMapController();
		mapController.addMapChangeListener(listener);
    }

	@Override
    protected Class<? extends IExtension> getExtensionClass() {
	    return AutomaticEdgeColor.class;
    }

	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		final int colorCount;
		if(element == null){
			colorCount = 0;
		}
		else{
			colorCount = element.getAttribute("COUNTER", 0);
		}
		
		return new AutomaticEdgeColor(colorCount);
	}

	@Override
    protected void saveExtension(IExtension extension, XMLElement element) {
	    super.saveExtension(extension, element);
	    final int colorCount = ((AutomaticEdgeColor)extension).getColorCount();
		element.setAttribute("COUNTER", Integer.toString(colorCount));
    }
}

class AutomaticEdgeColor implements IExtension{
	private int colorCount; 
	int getColorCount() {
    	return colorCount;
    }
	public AutomaticEdgeColor(int colorCount) {
	    super();
	    this.colorCount = colorCount;
    }
	private static final Color[] COLORS = new Color[]{
		Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN, Color.YELLOW, 
		Color.RED.darker().darker(), Color.BLUE.darker().darker(), Color.GREEN.darker().darker(), Color.MAGENTA.darker().darker(), Color.CYAN.darker().darker(), Color.YELLOW.darker().darker()};
	Color nextColor() {
		if(colorCount >= COLORS.length){
			colorCount = 0;
		}
		return COLORS[colorCount++]; 
    }
}

