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
package org.freeplane.features.nodelocation;

import java.util.Collection;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ExclusivePropertyChain;
import org.freeplane.features.mode.IPropertyHandler;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyleModel;

/**
 * @author Dimitry Polivaev
 */
public class LocationController implements IExtension {
	final private ExclusivePropertyChain<Quantity<LengthUnits>, NodeModel> childGapHandlers;
	public static LocationController getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return getController(modeController);
	}

	public static LocationController getController(ModeController modeController) {
		return modeController.getExtension(LocationController.class);
	}

	public static void install( final LocationController locationController) {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addExtension(LocationController.class, locationController);
	}

// 	final private ModeController modeController;

	public LocationController() {
		super();
//		this.modeController = modeController;
		final ModeController modeController = Controller.getCurrentModeController();
		final MapController mapController = modeController.getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		final LocationBuilder locationBuilder = new LocationBuilder();
		locationBuilder.registerBy(readManager, writeManager);
		childGapHandlers = new ExclusivePropertyChain<Quantity<LengthUnits>, NodeModel>();
		addChildGapGetter(IPropertyHandler.STYLE, new IPropertyHandler<Quantity<LengthUnits>, NodeModel>() {
			public Quantity<LengthUnits> getProperty(final NodeModel node, final Quantity<LengthUnits> currentValue) {
				final MapModel map = node.getMap();
				final LogicalStyleController styleController = LogicalStyleController.getController(modeController);
				final Collection<IStyle> style = styleController.getStyles(node);
				final Quantity<LengthUnits> returnedGap = getStyleChildGap(map, style);
				return returnedGap;
			}
		});
		addChildGapGetter(IPropertyHandler.DEFAULT, new IPropertyHandler<Quantity<LengthUnits>, NodeModel>() {
			public Quantity<LengthUnits> getProperty(final NodeModel node, final Quantity<LengthUnits> currentValue) {
				return LocationModel.DEFAULT_VGAP;
			}
		});

	}
	private IPropertyHandler<Quantity<LengthUnits>, NodeModel> addChildGapGetter(final Integer key,
            final IPropertyHandler<Quantity<LengthUnits>, NodeModel> getter) {
			return childGapHandlers.addGetter(key, getter);
	}

	private Quantity<LengthUnits> getStyleChildGap(final MapModel map, final Collection<IStyle> styleKeys) {
		final MapStyleModel model = MapStyleModel.getExtension(map);
		for(IStyle styleKey : styleKeys){
			final NodeModel styleNode = model.getStyleNode(styleKey);
			if (styleNode == null) {
				continue;
			}
			final LocationModel styleModel = styleNode.getExtension(LocationModel.class);
			if (styleModel == null) {
				continue;
			}
			Quantity<LengthUnits> vGap = styleModel.getVGap();
			if (vGap == LocationModel.DEFAULT_VGAP) {
				continue;
			}
			return vGap;
		}
		return null;
	}

	public Quantity<LengthUnits> getHorizontalShift(NodeModel node){
		return LocationModel.getModel(node).getHGap();
	}

	public Quantity<LengthUnits> getVerticalShift(NodeModel node){
		return LocationModel.getModel(node).getShiftY();
	}

	public Quantity<LengthUnits> getMinimalDistanceBetweenChildren(NodeModel node){
		return childGapHandlers.getProperty(node);
	}
}
