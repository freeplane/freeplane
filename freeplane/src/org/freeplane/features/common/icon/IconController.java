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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedSet;

import org.freeplane.core.controller.CombinedPropertyChain;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.IPropertyHandler;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.common.filter.FilterController;
import org.freeplane.features.common.icon.factory.IconStoreFactory;
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

	public IconController(final ModeController modeController) {
		super();
		iconHandlers = new CombinedPropertyChain<Collection<MindIcon>, NodeModel>();
//		this.modeController = modeController;
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final IconBuilder textBuilder = new IconBuilder(this, IconStoreFactory.create());
		textBuilder.registerBy(readManager, writeManager);
		addIconGetter(IPropertyHandler.NODE, new IPropertyHandler<Collection<MindIcon>, NodeModel>() {
			public Collection<MindIcon> getProperty(final NodeModel node, final Collection<MindIcon> currentValue) {
				final Collection<MindIcon> icons = node.getIcons();
				if (currentValue.isEmpty()) {
					return icons;
				}
				if (icons.isEmpty()) {
					return currentValue;
				}
				final ArrayList<MindIcon> arrayList = new ArrayList<MindIcon>(icons.size() + currentValue.size());
				arrayList.addAll(currentValue);
				arrayList.addAll(icons);
				return arrayList;
			}
		});
		addIconGetter(IPropertyHandler.STYLE, new IPropertyHandler<Collection<MindIcon>, NodeModel>() {
			public Collection<MindIcon> getProperty(final NodeModel node, final Collection<MindIcon> currentValue) {
				final MapStyleModel model = MapStyleModel.getExtension(node.getMap());
				final List<IStyle> styleKeys = LogicalStyleController.getController(modeController).getStyles(node);
				LinkedHashSet<MindIcon> icons = new LinkedHashSet<MindIcon>();
				for(IStyle styleKey : styleKeys){
					final NodeModel styleNode = model.getStyleNode(styleKey);
					if (styleNode == null) {
						continue;
					}
					final List<MindIcon> styleIcons;
					styleIcons = styleNode.getIcons();
					icons.addAll(styleIcons);
				}
				return icons;
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


	public static Collection<MindIcon> getIcons(final NodeModel node) {
		final IconController iconController = IconController.getController();
		final Collection<MindIcon> icons = iconController.iconHandlers.getProperty(node);
		return icons;
	}
}
