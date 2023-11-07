package org.freeplane.features.map.codeexplorermode;

import java.io.File;

import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.map.INodeDuplicator;
import org.freeplane.features.map.MapModel;

class CodeMapModel extends MapModel {
	public CodeMapModel(INodeDuplicator nodeDuplicator, final File[] roots) {
		super(nodeDuplicator);
		// create empty attribute registry
		AttributeRegistry.getRegistry(this);
		if(roots.length == 1)
			setRoot(new CodeNodeModel(roots[0], this));
		else
			setRoot(new CodeNodeModel(roots, this));
		getRootNode().setFolded(false);
	}

	@Override
	public String getTitle() {
		return "Code: " + getRootNode().toString();
	}
}
