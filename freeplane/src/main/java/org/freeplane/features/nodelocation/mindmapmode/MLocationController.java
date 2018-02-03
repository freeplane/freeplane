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
package org.freeplane.features.nodelocation.mindmapmode;

import java.util.ArrayList;

import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.map.IExtensionCopier;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodelocation.LocationController;
import org.freeplane.features.nodelocation.LocationModel;
import org.freeplane.features.styles.LogicalStyleKeys;

/**
 * @author Dimitry Polivaev
 */
public class MLocationController extends LocationController {
	
	private static class StyleCopier implements IExtensionCopier {
		public void copy(Object key, NodeModel from, NodeModel to) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			LocationModel source = from.getExtension(LocationModel.class);
			if(source != null){
				LocationModel.createLocationModel(to).setVGap(source.getVGap());
			}
		}

		public void remove(Object key, NodeModel from) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			LocationModel target = from.getExtension(LocationModel.class);
			if(target != null){
				target.setVGap(LocationModel.DEFAULT_VGAP);
			}
		}

		public void remove(Object key, NodeModel from, NodeModel which) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			LocationModel model = which.getExtension(LocationModel.class);
			if(model != null && model.getVGap() != LocationModel.DEFAULT_VGAP ){
				remove(key, from);
			}
		}

		@Override
		public void resolveParentExtensions(Object key, NodeModel to) {
		}
	}
	
	public MLocationController() {
		super();
		final ModeController modeController = Controller.getCurrentModeController();
		createActions(modeController);
		modeController.registerExtensionCopier(new StyleCopier());
	}

	private void createActions(ModeController modeController) {
		modeController.addAction(new ResetNodeLocationAction());
	}

	public void moveNodePosition(final NodeModel node, final Quantity<LengthUnits> hGap, final Quantity<LengthUnits> shiftY) {
		final ModeController currentModeController = Controller.getCurrentModeController();
		MapModel map = node.getMap();
		ArrayList<IActor> actors = new ArrayList<IActor>(3);
		final LocationModel locationModel = LocationModel.getModel(node);
		Quantity<LengthUnits> oldHGap = locationModel.getHGap();
		if(! hGap.equals(oldHGap))
			actors.add(new ChangeShiftXActor(node, hGap));
		Quantity<LengthUnits> oldVerticalShift = locationModel.getShiftY();
		if(! shiftY.equals(oldVerticalShift))
		actors.add(new ChangeShiftYActor(node, shiftY));
		for (final IActor actor : actors) {
			currentModeController.execute(actor, map);
		}
	}

	public void setHorizontalShift(NodeModel node, final Quantity<LengthUnits> horizontalShift){
		final LocationModel locationModel = LocationModel.getModel(node);
		Quantity<LengthUnits> oldHGap = locationModel.getHGap();
		if(! horizontalShift.equals(oldHGap)) {
			final IActor actor = new ChangeShiftXActor(node, horizontalShift);
			Controller.getCurrentModeController().execute(actor, node.getMap());
		}
	}

	public void setVerticalShift(NodeModel node, final Quantity<LengthUnits> verticalShift){
		final LocationModel locationModel = LocationModel.getModel(node);
		Quantity<LengthUnits> oldVerticalShift = locationModel.getShiftY();
		if(! verticalShift.equals(oldVerticalShift)) {
			final IActor actor = new ChangeShiftYActor(node, verticalShift);
			Controller.getCurrentModeController().execute(actor, node.getMap());
		}
	}

	public void setMinimalDistanceBetweenChildren(NodeModel node, final Quantity<LengthUnits> minimalDistanceBetweenChildren){
		if(node != null){
			Quantity.assertNonNegativeOrNull(minimalDistanceBetweenChildren);
			final LocationModel locationModel = LocationModel.getModel(node);
			Quantity<LengthUnits> oldVgap = locationModel.getVGap();
			if(! minimalDistanceBetweenChildren.equals(oldVgap)) {
				final IActor actor = new ChangeVGapActor(node, minimalDistanceBetweenChildren);
				Controller.getCurrentModeController().execute(actor, node.getMap());
			}
		}

	}
}
