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
package org.freeplane.features.edge;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.features.edge.AutomaticEdgeColor.Rule;
import org.freeplane.features.edge.mindmapmode.MEdgeController;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeMoveEvent;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.styles.LogicalStyleController;
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

	private class Listener implements IMapChangeListener {
		@Override
	    public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {
			if(!isActiveOnCreation(child) || modeController.isUndoAction()){
				return;
			}
			if(MapStyleModel.FLOATING_STYLE.equals(LogicalStyleModel.getStyle(child)))
				return;
			if(parent.isRoot()){
				final EdgeModel edgeModel = EdgeModel.createEdgeModel(child);
				if(null == edgeModel.getColor()){
					final MEdgeController controller = (MEdgeController) EdgeController.getController();
					final MapModel map = parent.getMap();
					final AutomaticEdgeColor model = (AutomaticEdgeColor) getMapHook(map);
					model.increaseColorCounter();
					if(controller.areEdgeColorsAvailable(map)){
						int colorCounter = model.getColorCounter();
						controller.setColor(child, controller.getEdgeColor(map, colorCounter));
					}
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

        private boolean isActiveOnCreation(NodeModel node) {
			final AutomaticEdgeColor extension = node.getMap().getRootNode().getExtension(AutomaticEdgeColor.class);
			return extension != null && extension.rule.isActiveOnCreation;
		}

		@Override
        public void onNodeMoved(NodeMoveEvent nodeMoveEvent) {
            onNodeInserted(nodeMoveEvent.newParent, nodeMoveEvent.child, nodeMoveEvent.newIndex);
        }
	}

	public AutomaticEdgeColorHook() {
	    super();
		final Listener listener = new Listener();
		modeController = Controller.getCurrentModeController();
		modeController.addExtension(AutomaticEdgeColorHook.class, this);

		final MapController mapController = modeController.getMapController();
		mapController.addUIMapChangeListener(listener);
    }

	@Override
	protected void registerActions() {
	}



	@Override
    protected Class<? extends IExtension> getExtensionClass() {
	    return AutomaticEdgeColor.class;
    }

	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		final int colorCount = element == null ? 0 : element.getAttribute("COUNTER", 0);
		final Rule rule;
		if (element == null)
			rule = Rule.ON_BRANCH_CREATION;
		else
			rule = safeValueOf(element.getAttribute("RULE", null), Rule.ON_BRANCH_CREATION);
		return new AutomaticEdgeColor(rule, colorCount);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T safeValueOf(final String value, T defaultValue) {
		try {
			return value == null ? defaultValue : (T) Enum.valueOf(defaultValue.getClass(), value);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	@Override
	protected void saveExtension(IExtension extension, XMLElement element) {
		final AutomaticEdgeColor automaticEdgeColor = (AutomaticEdgeColor)extension;
		super.saveExtension(extension, element);
		final int colorCount = automaticEdgeColor.getColorCounter();
		element.setAttribute("COUNTER", Integer.toString(colorCount));
		element.setAttribute("RULE", automaticEdgeColor.rule.toString());
	}

	@Override
    protected IExtension toggle(NodeModel node, IExtension extension) {
		extension = super.toggle(node, extension);
	    final MModeController modeController = (MModeController) Controller.getCurrentModeController();
	    if(modeController.isUndoAction()){
	    	return extension;
	    }
	    LogicalStyleController.getController().refreshMap(node.getMap());
    	return extension;
    }

}

