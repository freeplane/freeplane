package org.freeplane.features.common.map;

import org.freeplane.core.addins.NodeHookDescriptor;
import org.freeplane.core.addins.PersistentNodeHook;
import org.freeplane.core.extension.IExtension;
import org.freeplane.n3.nanoxml.XMLElement;

@NodeHookDescriptor(hookName = "BranchesOverlap")
public class BranchesOverlap extends PersistentNodeHook implements IExtension {

	@Override
	protected IExtension createExtension(NodeModel node, XMLElement element) {
		return this;
	}
	
	static public boolean isEnabled(MapModel map){
		return map.getRootNode().containsExtension(BranchesOverlap.class);
	}

}
