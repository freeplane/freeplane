package org.freeplane.core.ui.ribbon;

import java.util.Enumeration;

import org.freeplane.core.ui.ribbon.StructureTree.StructurePath;
import org.freeplane.core.ui.ribbon.StructureTree.StructureTreeHull;

public class RibbonBuildContext {
	private final RibbonBuilder ribbonBuilder;
	private StructurePath currentPath;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public RibbonBuildContext(RibbonBuilder builder) {
		this.ribbonBuilder = builder;
		currentPath = builder.structure.getRootPath();
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public RibbonBuilder getBuilder() {
		return ribbonBuilder;
	}
	
	public void processChildren(StructurePath path, ARibbonContributor parent) {
		WeightedMutableTreeNode<StructureTreeHull> parentNode = ribbonBuilder.structure.get(path);
		if(parentNode != null) {
			StructurePath lastPath = getCurrentPath();
			try {
				Enumeration<WeightedMutableTreeNode<StructureTreeHull>> children = parentNode.children();
				while (children.hasMoreElements()) {
					WeightedMutableTreeNode<StructureTreeHull> node = children.nextElement();
					currentPath = node.getUserObject().getPath();
					((ARibbonContributor) node.getUserObject().getObject()).contribute(this, parent);
				}
			}
			finally {
				currentPath = lastPath;
			}
		}
	}
	
	public StructurePath getCurrentPath() {
		return this.currentPath;
	}
	
	public StructurePath getPathForObject(Object obj) {
		return ribbonBuilder.structure.getPathByUserObject(obj);
	}
	
	public boolean hasChildren(StructurePath path) {
		WeightedMutableTreeNode<StructureTreeHull> node = ribbonBuilder.structure.get(path);
		if(node != null) {
			return !node.isLeaf();
		}
		return false;
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
