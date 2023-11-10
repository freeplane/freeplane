package org.freeplane.features.map.codeexplorermode;

import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.map.INodeDuplicator;
import org.freeplane.features.map.MapModel;

import com.tngtech.archunit.core.domain.JavaPackage;

class CodeMapModel extends MapModel {
	public CodeMapModel(INodeDuplicator nodeDuplicator, final JavaPackage rootPackage) {
		super(nodeDuplicator);
		// create empty attribute registry
		AttributeRegistry.getRegistry(this);

		setRoot(new JavaPackageNodeModel(rootPackage, this, true));
		getRootNode().setFolded(false);
	}

	@Override
	public String getTitle() {
		return "Code: " + getRootNode().toString();
	}
}
