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
import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.IPropertyHandler;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.n3.nanoxml.XMLElement;

@NodeHookDescriptor(hookName = "accessories/plugins/AutomaticLayout.properties", onceForMap = true)
public class AutomaticLayoutController extends PersistentNodeHook implements IExtension{
	private static final String AUTOMATIC_LAYOUT_LEVEL = "AutomaticLayout.level,";
	private static final String AUTOMATIC_LAYOUT_LEVEL_ROOT = "AutomaticLayout.level.root";

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
		if(layout == null || node.isLeaf() && ! layout.applyToLeaves)
			return null;
		final int depth = node.depth();
		return getStyle(node.getMap(), depth);
	}

	public IStyle getStyle(final MapModel map, final int depth) {
		final MapStyleModel extension = MapStyleModel.getExtension(map);
		final String name = depth == 0 ? AUTOMATIC_LAYOUT_LEVEL_ROOT : AUTOMATIC_LAYOUT_LEVEL + depth;
		final TranslatedObject styleKey = TranslatedObject.format(name);
		final IStyle style = StyleFactory.create(styleKey);
		if (extension.getStyleNode(style) != null) {
			return style;
		}
		else
			return null;
	}
	
	public NodeModel getStyleNode(MapModel map, int depth) {
		IStyle style = getStyle(map, depth);
		if(style != null){
			final MapStyleModel extension = MapStyleModel.getExtension(map);
			return extension.getStyleNode(style);
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

	public boolean isAutomaticLevelStyle(NodeModel styleNode) {
		NodeModel parentNode = styleNode.getParentNode();
		if (parentNode == null)
			return false;
		Object userObject = parentNode.getUserObject();
		if (! (userObject instanceof StyleTranslatedObject))
			return false;
		return ((StyleTranslatedObject)userObject).getObject().equals(MapStyleModel.STYLES_AUTOMATIC_LAYOUT);
	}

}
