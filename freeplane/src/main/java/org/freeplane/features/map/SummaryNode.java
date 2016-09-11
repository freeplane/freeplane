/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.features.map;	

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Apr 9, 2011
 */
@NodeHookDescriptor(hookName = "SummaryNode", onceForMap = false)
public class SummaryNode extends PersistentNodeHook implements IExtension{
	
	private ModeController modeController;

	public static void install(){
		new SummaryNode();
		new FirstGroupNode();
	};
	
	static public boolean isFirstGroupNode(final NodeModel nodeModel) {
		return nodeModel.containsExtension(FirstGroupNodeFlag.class);
	}
	
	

	public SummaryNode() {
		super();
		modeController = Controller.getCurrentModeController();
	}

	@Override
	protected IExtension createExtension(NodeModel node, XMLElement element) {
		return SummaryNodeFlag.SUMMARY;
	}
	
	@Override
	protected Class<? extends IExtension> getExtensionClass() {
		return SummaryNodeFlag.class;
	}
	
	@Override
	protected HookAction createHookAction() {
		return null;
	}

	static public boolean isSummaryNode(final NodeModel nodeModel) {
		return nodeModel.containsExtension(SummaryNodeFlag.class);
	}

	static public NodeModel getRealNode(final NodeModel nodeModel) {
		if (!isSummaryNode(nodeModel))
			return nodeModel;
		for(int i = nodeModel.getChildCount() - 1; i >= 0; i--){
			final NodeModel child = nodeModel.getChildAt(i);
			if(! isHidden(child)) {
				return child;
			}
		}
		return nodeModel;
	}
	
	static public boolean isHidden(final NodeModel nodeModel) {
		return ! nodeModel.isFolded()  && (nodeModel.hasChildren() && isSummaryNode(nodeModel) || isFirstGroupNode(nodeModel))&& nodeModel.getText().isEmpty(); 
	}

	public static int getSummaryLevel(NodeModel node) {
		if(node.isRoot() || ! isSummaryNode(node))
			return 0;
		final NodeModel parentNode = node.getParentNode();
		final int index = parentNode.getIndex(node);
		final boolean isleft = node.isLeft();
		int level = 1;
		for(int i =  index - 1; i > 0; i--){
			final NodeModel child = (NodeModel) parentNode.getChildAt(i);
			if(isleft == child.isLeft()){
				if( isSummaryNode(child))
					level++;
				else
					return level;
			}
		}
		return level;
    }
}

