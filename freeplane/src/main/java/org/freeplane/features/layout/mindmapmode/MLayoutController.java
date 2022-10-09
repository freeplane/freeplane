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
package org.freeplane.features.layout.mindmapmode;

import org.freeplane.api.ChildNodesAlignment;
import org.freeplane.api.ChildrenSides;
import org.freeplane.api.LayoutOrientation;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.layout.LayoutController;
import org.freeplane.features.layout.LayoutModel;
import org.freeplane.features.map.IExtensionCopier;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.LogicalStyleKeys;
/**
 * @author Dimitry Polivaev
 */
public class MLayoutController extends LayoutController {
	
	private static class StyleCopier implements IExtensionCopier {
		@Override
        public void copy(Object key, NodeModel from, NodeModel to) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			LayoutModel source = from.getExtension(LayoutModel.class);
			if(source != null){
				LayoutModel layoutModel = LayoutModel.createLayoutModel(to);
				layoutModel.setChildNodesAlignment(layoutModel.getChildNodesAlignment());
				layoutModel.setLayoutOrientation(layoutModel.getLayoutOrientation());
				layoutModel.setChildrenSides(layoutModel.getChildrenSides());
			}
		}

		@Override
        public void remove(Object key, NodeModel from) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			LayoutModel target = from.getExtension(LayoutModel.class);
			if(target != null){
                target.setChildNodesAlignment(LayoutModel.DEFAULT_CHILD_NODES_ALIGNMENT);
                target.setLayoutOrientation(LayoutOrientation.NOT_SET);
                target.setChildrenSides(ChildrenSides.NOT_SET);
			}
		}

		@Override
        public void remove(Object key, NodeModel from, NodeModel which) {
			if (!key.equals(LogicalStyleKeys.NODE_STYLE)) {
				return;
			}
			LayoutModel model = which.getExtension(LayoutModel.class);
            if(model == null)
                return;
			LayoutModel target = from.getExtension(LayoutModel.class);
            if(target == null)
                return;
            if(model.getChildNodesAlignment() != LayoutModel.DEFAULT_CHILD_NODES_ALIGNMENT ){
                target.setChildNodesAlignment(LayoutModel.DEFAULT_CHILD_NODES_ALIGNMENT);
            }
            if(model.getLayoutOrientation() != LayoutOrientation.NOT_SET ){
                target.setLayoutOrientation(LayoutOrientation.NOT_SET);
            }
            if(model.getChildrenSides() != ChildrenSides.NOT_SET ){
                target.setChildrenSides(ChildrenSides.NOT_SET);
            }
		}
	}
	
	public MLayoutController() {
		super();
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.registerExtensionCopier(new StyleCopier());
	}

    public void setChildNodesAlignment(NodeModel node, ChildNodesAlignment alignment){
        if(node != null){
            final IActor actor = new ChangeChildNodesAlignmentActor(node, alignment != null ? alignment : LayoutModel.DEFAULT_CHILD_NODES_ALIGNMENT);
            Controller.getCurrentModeController().execute(actor, node.getMap());
        }
    }

    public void setLayoutOrientation(NodeModel node, LayoutOrientation orientation){
        if(node != null){
            final IActor actor = new ChangeLayoutOrientationActor(node, orientation != null ? orientation : LayoutOrientation.NOT_SET);
            Controller.getCurrentModeController().execute(actor, node.getMap());
        }
    }

    public void setChildrenSides(NodeModel node, ChildrenSides sides){
        if(node != null){
            final IActor actor = new ChangeChildrenSidesActor(node, sides != null ? sides : ChildrenSides.NOT_SET);
            Controller.getCurrentModeController().execute(actor, node.getMap());
        }
    }
}
