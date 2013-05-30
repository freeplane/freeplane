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
package org.freeplane.features.styles;

import java.util.Collection;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.IPropertyHandler;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.n3.nanoxml.XMLElement;

@NodeHookDescriptor(hookName = "accessories/plugins/AutomaticLayout.properties")
public class AutomaticLayoutController extends PersistentNodeHook implements IExtension{
	/**
	 *
	 */
	public AutomaticLayoutController() {
		super();
		LogicalStyleController.getController().addStyleGetter(IPropertyHandler.AUTO, new IPropertyHandler<Collection<IStyle>, NodeModel>() {
			public Collection<IStyle> getProperty(NodeModel model, Collection<IStyle> currentValue) {
				AutomaticLayout layout = model.getMap().getRootNode().getExtension(AutomaticLayout.class);
				final IStyle autoStyle = getStyle(model, layout);
				if(autoStyle != null){
					LogicalStyleController.getController().add(model, currentValue, autoStyle);
				}
				return currentValue;
			}
		});
	}

	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		if(element == null || ! element.hasAttribute("VALUE"))
			return AutomaticLayout.ALL;
		return super.createExtension(node, element);
	}

	private IStyle getStyle(final NodeModel node, AutomaticLayout layout) {
		if(layout == null || node.isLeaf() && ! layout.equals(AutomaticLayout.ALL))
			return null;
		final int depth = node.depth();
		final MapModel map = node.getMap();
		final MapStyleModel extension = MapStyleModel.getExtension(map);
		final String name = depth == 0 ? "AutomaticLayout.level.root" : "AutomaticLayout.level," + depth;
		final NamedObject obj = NamedObject.format(name);
		final IStyle style = StyleFactory.create(obj);
		if (extension.getStyleNode(style) != null) {
			return style;
		}
		return null;
	}

	@Override
	protected Class<? extends IExtension> getExtensionClass() {
		return AutomaticLayout.class;
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
