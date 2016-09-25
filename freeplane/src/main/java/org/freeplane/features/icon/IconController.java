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
package org.freeplane.features.icon;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.CombinedPropertyChain;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.IPropertyHandler;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeSizeModel;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.StyleNode;

/**
 * @author Dimitry Polivaev
 */
public class IconController implements IExtension {

	private static final Quantity<LengthUnits> DEFAULT_ICON_SIZE = new Quantity<LengthUnits>(16, LengthUnits.px);

	final private CombinedPropertyChain<Collection<MindIcon>, NodeModel> iconHandlers;
	public static IconController getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return getController(modeController);
	}
	public static IconController getController(ModeController modeController) {
		return (IconController) modeController.getExtension(IconController.class);
    }

	public static void install() {
		final ConditionFactory conditionFactory = FilterController.getCurrentFilterController().getConditionFactory();
		conditionFactory.addConditionController(10, new IconConditionController());
		conditionFactory.addConditionController(50, new PriorityConditionController());
	}

	public static void install( final IconController iconController) {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addExtension(IconController.class, iconController);
	}

// 	final private ModeController modeController;
	final private Collection<IStateIconProvider> stateIconProviders;
	
	final private List<IconMouseListener> iconMouseListeners;
	
	public void addIconMouseListener(final IconMouseListener iconMouseListener) {
		iconMouseListeners.add(iconMouseListener);
	}

	public boolean addStateIconProvider(IStateIconProvider o) {
	    return stateIconProviders.add(o);
    }
	public boolean removeStateIconProvider(IStateIconProvider o) {
	    return stateIconProviders.remove(o);
    }
	public IconController(final ModeController modeController) {
		super();
		stateIconProviders = new LinkedList<IStateIconProvider>();
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
					if (styleNode == null || node == styleNode && !(styleKey instanceof StyleNode)) {
						continue;
					}
					final List<MindIcon> styleIcons;
					styleIcons = styleNode.getIcons();
					currentValue.addAll(styleIcons);
				}
				return currentValue;
			}
		});
		iconMouseListeners = new LinkedList<IconMouseListener>();
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
	
	public final Collection<UIIcon> getStateIcons(final NodeModel node){
		final LinkedList<UIIcon> icons = new LinkedList<UIIcon>();
		for(IStateIconProvider provider : stateIconProviders){
			final UIIcon stateIcon = provider.getStateIcon(node);
			if(stateIcon != null){
				icons.add(stateIcon);
				final IconRegistry iconRegistry = node.getMap().getIconRegistry();
				iconRegistry.addIcon(stateIcon);
			}
		}
		return icons;
	}
	public boolean onIconClicked(NodeModel node, UIIcon icon) {
		boolean processed = false;
		for (IconMouseListener listener : iconMouseListeners)
		{
			final IconClickedEvent event = new IconClickedEvent(icon, node);
			if(listener.onIconClicked(event)) {
				processed = true;
			}
		}
		return processed;
	}

	private Quantity<LengthUnits> getStyleIconSize(final MapModel map, final Collection<IStyle> styleKeys) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : styleKeys){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final Quantity<LengthUnits> iconSize = styleNode.getSharedData().getIcons().getIconSize();
			if (iconSize == null) {
				continue;
			}
			return iconSize;
		}
		return DEFAULT_ICON_SIZE;
	}

	public Quantity<LengthUnits> getIconSize(NodeModel node)
	{
		final MapModel map = node.getMap();
		final ModeController modeController = Controller.getCurrentModeController();
		final LogicalStyleController styleController = LogicalStyleController.getController(modeController);
		final Collection<IStyle> styles = styleController.getStyles(node);
		final Quantity<LengthUnits> minWidth = getStyleIconSize(map, styles);
		return minWidth;
	}

}
