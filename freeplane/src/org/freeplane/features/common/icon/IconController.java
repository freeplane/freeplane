/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.features.common.icon;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;

import org.freeplane.core.controller.CombinedPropertyChain;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.IPropertyHandler;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.features.common.filter.FilterController;
import org.freeplane.features.common.icon.factory.IconStoreFactory;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.map.MapController;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.styles.IStyle;
import org.freeplane.features.common.styles.LogicalStyleController;
import org.freeplane.features.common.styles.MapStyleModel;

/**
 * @author Dimitry Polivaev
 */
public class IconController implements IExtension {
	final private CombinedPropertyChain<Collection<MindIcon>, NodeModel> iconHandlers;
	public static IconController getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return getController(modeController);
	}
	public static IconController getController(ModeController modeController) {
		return (IconController) modeController.getExtension(IconController.class);
    }

	public static void install() {
		FilterController.getCurrentFilterController().getConditionFactory().addConditionController(1,
		    new IconConditionController());
		FilterController.getCurrentFilterController().getConditionFactory().addConditionController(5,
		    new PriorityConditionController());
	}

	public static void install( final IconController iconController) {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addExtension(IconController.class, iconController);
	}

// 	final private ModeController modeController;

	public static final String LINK_ICON = ResourceController.getResourceController().getProperty("link_icon");
	private static final String MENUITEM_ICON = "icons/button.png";
	private static final String EXECUTABLE_ICON = ResourceController.getResourceController().getProperty(
	    "executable_icon");
	private static final String MAIL_ICON = ResourceController.getResourceController().getProperty("mail_icon");
	private static final String LINK_LOCAL_ICON = ResourceController.getResourceController().getProperty(
	    "link_local_icon");
	
	private static final IconStore STORE = IconStoreFactory.create();
	public static Icon getLinkIcon(final URI link, final NodeModel model) {
		if (link == null) 
			return null;
	    final String linkText = link.toString();
	    final String iconPath;
	    if (linkText.startsWith("#")) {
	    	final String id = linkText.substring(1);
	    	if (model == null || model.getMap().getNodeForID(id) == null) {
	    		iconPath = null;
	    	}
	    	else{
	    		iconPath = LINK_LOCAL_ICON;
	    	}
	    }
	    else if (linkText.startsWith("mailto:")) {
	    	iconPath = MAIL_ICON;
	    }
	    else if (Compat.executableExtensions.contains(link)) {
	    	iconPath = EXECUTABLE_ICON;
	    }
	    else if (LinkController.isMenuItemLink(link)) {
	    	// nodes with menu item link contain the image from the menu if available
	    	if (model == null || model.getIcons().isEmpty())
	    		iconPath = MENUITEM_ICON;
	    	else
	    		iconPath = null;
	    }
	    else if (Compat.isExecutable(linkText)) {
	    	iconPath = "Executable.png";
	    }
	    else{
	    	iconPath = IconController.LINK_ICON;
	    }
	    if(iconPath == null)
	    	return null;
	    final UIIcon uiIcon = STORE.getUIIcon(iconPath);
	    if(uiIcon == null)
	    	return null;
	    return uiIcon.getIcon();
    }


	public IconController(final ModeController modeController) {
		super();
		iconHandlers = new CombinedPropertyChain<Collection<MindIcon>, NodeModel>(false);
//		this.modeController = modeController;
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final IconBuilder textBuilder = new IconBuilder(this, IconStoreFactory.create());
		textBuilder.registerBy(readManager, writeManager);
		addIconGetter(IPropertyHandler.STYLE, new IPropertyHandler<Collection<MindIcon>, NodeModel>() {
			public Collection<MindIcon> getProperty(final NodeModel node, final Collection<MindIcon> currentValue) {
				final MapStyleModel model = MapStyleModel.getExtension(node.getMap());
				final Collection<IStyle> styleKeys = LogicalStyleController.getController(modeController).getStyles(node);
				for(IStyle styleKey : styleKeys){
					final NodeModel styleNode = model.getStyleNode(styleKey);
					if (styleNode == null) {
						continue;
					}
					final List<MindIcon> styleIcons;
					styleIcons = styleNode.getIcons();
					currentValue.addAll(styleIcons);
				}
				return currentValue;
			}
		});
	}

	public IPropertyHandler<Collection<MindIcon>, NodeModel> addIconGetter(
	                                                                 final Integer key,
	                                                                 final IPropertyHandler<Collection<MindIcon>, NodeModel> getter) {
		return iconHandlers.addGetter(key, getter);
	}

	public IPropertyHandler<Collection<MindIcon>, NodeModel> removeIconGetter(
	                                                                    final Integer key,
	                                                                    final IPropertyHandler<Collection<MindIcon>, NodeModel> getter) {
		return iconHandlers.addGetter(key, getter);
	}


	public Collection<MindIcon> getIcons(final NodeModel node) {
		final Collection<MindIcon> icons = iconHandlers.getProperty(node, new LinkedList<MindIcon>());
		return icons;
	}

}
