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

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ExclusivePropertyChain;
import org.freeplane.features.mode.IPropertyHandler;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;
import org.freeplane.features.styles.MapStyleModel;

/**
 * @author Dimitry Polivaev
 */
public class LocationController implements IExtension {
	final private ExclusivePropertyChain<Quantity<LengthUnit>, NodeModel> childVGapHandlers;
    final private ExclusivePropertyChain<Quantity<LengthUnit>, NodeModel> childHGapHandlers;

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
        childVGapHandlers = new ExclusivePropertyChain<Quantity<LengthUnit>, NodeModel>();
        childHGapHandlers = new ExclusivePropertyChain<Quantity<LengthUnit>, NodeModel>();
		childVGapHandlers.addGetter(IPropertyHandler.STYLE, (node, option, currentValue) -> {
        	final MapModel map = node.getMap();
        	final LogicalStyleController styleController = LogicalStyleController.getController(modeController);
        	final Collection<IStyle> style = styleController.getStyles(node, StyleOption.FOR_UNSELECTED_NODE);
        	final Quantity<LengthUnit> returnedGap = getStyleChildVGap(map, style);
        	return returnedGap;
        });
		childVGapHandlers.addGetter(IPropertyHandler.DEFAULT, (node, option, currentValue) -> LocationModel.DEFAULT_VGAP);
		childHGapHandlers.addGetter(IPropertyHandler.STYLE, (node, option, currentValue) -> {
            final MapModel map = node.getMap();
            final LogicalStyleController styleController = LogicalStyleController.getController(modeController);
            final Collection<IStyle> style = styleController.getStyles(node, StyleOption.FOR_UNSELECTED_NODE);
            final Quantity<LengthUnit> returnedGap = getStyleCommonChildHGap(map, style);
            return returnedGap;
        });
		childHGapHandlers.addGetter(IPropertyHandler.DEFAULT, (node, option, currentValue) -> LocationModel.DEFAULT_HGAP);

	}
	private Quantity<LengthUnit> getStyleChildVGap(final MapModel map, final Collection<IStyle> styleKeys) {
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
            Quantity<LengthUnit> vGap = styleModel.getVGap();
            if (vGap == LocationModel.DEFAULT_VGAP) {
                continue;
            }
            return vGap;
        }
        return null;
    }

    private Quantity<LengthUnit> getStyleCommonChildHGap(final MapModel map, final Collection<IStyle> styleKeys) {
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
            Quantity<LengthUnit> gap = styleModel.getBaseHGap();
            if (gap == LocationModel.DEFAULT_HGAP) {
                continue;
            }
            return gap;
        }
        return null;
    }

	public Quantity<LengthUnit> getHorizontalShift(NodeModel node){
		return LocationModel.getModel(node).getHGap();
	}

	public Quantity<LengthUnit> getVerticalShift(NodeModel node){
		return LocationModel.getModel(node).getShiftY();
	}

    public Quantity<LengthUnit> getCommonVGapBetweenChildren(NodeModel node){
        return childVGapHandlers.getProperty(node, StyleOption.FOR_UNSELECTED_NODE);
    }

    public Quantity<LengthUnit> getBaseHGapToChildren(NodeModel node){
        return childHGapHandlers.getProperty(node, StyleOption.FOR_UNSELECTED_NODE);
    }
}
