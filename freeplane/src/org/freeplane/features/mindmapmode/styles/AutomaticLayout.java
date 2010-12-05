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
package org.freeplane.features.mindmapmode.styles;

import java.util.Collection;
import org.freeplane.core.addins.NodeHookDescriptor;
import org.freeplane.core.addins.PersistentNodeHook;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.IPropertyHandler;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.styles.IStyle;
import org.freeplane.features.common.styles.LogicalStyleController;
import org.freeplane.features.common.styles.MapStyleModel;
import org.freeplane.features.common.styles.StyleFactory;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.n3.nanoxml.XMLElement;

@NodeHookDescriptor(hookName = "accessories/plugins/AutomaticLayout.properties")
@ActionLocationDescriptor(locations = "/menu_bar/format/nodes")
public class AutomaticLayout extends PersistentNodeHook implements IExtension {

	/**
	 *
	 */
	public AutomaticLayout() {
		super();
		LogicalStyleController.getController().addStyleGetter(IPropertyHandler.AUTO, new IPropertyHandler<Collection<IStyle>, NodeModel>() {
			public Collection<IStyle> getProperty(NodeModel model, Collection<IStyle> currentValue) {
				if(model.getMap().getRootNode().containsExtension(AutomaticLayout.class)){
					currentValue.add(getStyle(model));
				}
				return currentValue;
			}
		});
	}

	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		return this;
	}

	private IStyle getStyle(final NodeModel node) {
		final int depth = node.depth();
		final MapModel map = node.getMap();
		final MapStyleModel extension = MapStyleModel.getExtension(map);
		final String name = depth == 0 ? "AutomaticLayout.level.root" : "AutomaticLayout.level," + depth;
		final NamedObject obj = NamedObject.format(name);
		final IStyle style = StyleFactory.create(obj);
		if (extension.getStyleNode(style) != null) {
			return style;
		}
		return MapStyleModel.DEFAULT_STYLE;
	}

	@Override
    protected void toggle(NodeModel node, IExtension extension) {
	    super.toggle(node, extension);
	    final MModeController modeController = (MModeController) Controller.getCurrentModeController();
	    if(modeController.isUndoAction()){
	    	return;
	    }
	    LogicalStyleController.getController().refreshMap(node.getMap());
    }
	
}
