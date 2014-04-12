package org.freeplane.core.ui.ribbon;

import java.util.Enumeration;

public class StructureTree {

	private final WeightedMutableTreeNode<StructureTreeHull> root = new WeightedMutableTreeNode<StructureTree.StructureTreeHull>();
	public static final StructurePath ROOT_PATH = new RootStructurePath();
	
	public static final int FIRST = WeightedMutableTreeNode.FIRST;
	public static final int PREPEND = WeightedMutableTreeNode.PREPEND;
	public static final int APPEND = WeightedMutableTreeNode.APPEND;
	public static final int LAST = WeightedMutableTreeNode.LAST;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public StructureTree() {
		root.setUserObject(new StructureTreeHull(null, ROOT_PATH));
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public boolean insert(StructurePath path, Object obj, int position) {
		if(path == null || obj == null) {
			return false;
		}
		StructureTreeHull hull = new StructureTreeHull(obj, path);
		WeightedMutableTreeNode<StructureTreeHull> newChild = new WeightedMutableTreeNode<StructureTree.StructureTreeHull>(hull, position);
		
		WeightedMutableTreeNode<StructureTreeHull> child = get(path.getParent());
		if(child != null) {
			child.addChild(newChild);
			return true;
		}
		return false;
	}
	
	public Object getObject(StructurePath path) {
		return get(root, path).getUserObject().getObject();
	}
	
	public WeightedMutableTreeNode<StructureTreeHull> get(StructurePath path) {
		return get(root, path);
	}

	public boolean contains(StructurePath path) {
		return get(root, path) != null;
	}
	
	public StructurePath getRootPath() {
		return ROOT_PATH;
	}
	
	public StructurePath getPathByUserObject(Object userObject) {
		return findRecursive(root, userObject);
	}

	private StructurePath findRecursive(WeightedMutableTreeNode<StructureTreeHull> parent, Object userObject) {
		Enumeration<WeightedMutableTreeNode<StructureTreeHull>> children = parent.children();
		StructurePath path = null;
		while (path == null && children.hasMoreElements()) {
			WeightedMutableTreeNode<StructureTreeHull> child = children.nextElement();
			if (userObject == child.getUserObject().getObject()) {
				return child.getUserObject().getPath();
			}
			else {
				path = findRecursive(child, userObject);
			}
		}
		return path;
	}
	
	private WeightedMutableTreeNode<StructureTreeHull> get(WeightedMutableTreeNode<StructureTreeHull> parent, StructurePath path) {
		if(path != null) {
			if(getRootPath().equals(path)) {
				return root;
			}
			Enumeration<WeightedMutableTreeNode<StructureTreeHull>> children = parent.children();
			while (children.hasMoreElements()) {
				WeightedMutableTreeNode<StructureTreeHull> child = children.nextElement();
				if(path.equals(child.getUserObject().getPath())) {
					return child;
				}
				else if (path.isAncestor(child.getUserObject().getPath())) {
					return get(child, path);
				} 
			}
		}
		return null;
	}

	/***********************************************************************************
	 * NESTED TYPE DECLARATION
	 **********************************************************************************/

	public static class StructurePath {
		private final StructurePath parentPath;
		private final String id;

		public StructurePath(StructurePath parent, String id) {
			if (id == null || id.trim().isEmpty()) {
				throw new IllegalArgumentException("id cannot be NULL or EMPTY");
			}
			this.id = id;
			this.parentPath = parent;
		}

		public String getId() {
			return this.id;
		}

		public StructurePath getParent() {
			return parentPath;
		}

		public StructurePath clone() {
			try {
				return (StructurePath) super.clone();
			} catch (CloneNotSupportedException e) {
			}
			return null;
		}

		public boolean equals(Object o) {
			if (o instanceof StructurePath) {
				return toString().equals(o.toString());
			}
			return false;
		}

		public boolean isAncestor(StructurePath ancestor) {
			if (ancestor != null) {
				if (this.equals(ancestor)) {
					return true;
				}
				else if(getParent() != null){
					return getParent().isAncestor(ancestor);
				}
			}
			return false;
		}

		public String toString() {
			return (parentPath != null ? parentPath.toString() : "") + "/" + getId();
		}
	}
	
	private static class RootStructurePath extends StructurePath {

		public RootStructurePath() {
			super(null, "root");
		}

		public String getId() {
			return "";
		}

		public boolean equals(Object o) {
			if (o instanceof StructurePath) {
				StructurePath other = (StructurePath) o;
				return other.getParent() == null && other.getId().isEmpty();
			}
			return false;
		}

		public boolean isAncestor(StructurePath ancestor) {
			return false;
		}

		public String toString() {
			return "";
		}
	}


	public static class StructureTreeHull {
		private Object object;
		private final StructurePath path;

		/***********************************************************************************
		 * CONSTRUCTORS
		 **********************************************************************************/

		public StructureTreeHull(Object obj, StructurePath path) {
			this.path = path;
			setObject(obj);
		}

		/***********************************************************************************
		 * METHODS
		 **********************************************************************************/
		public StructurePath getPath() {
			return path;
		}

		public void setObject(Object obj) {
			this.object = obj;
		}

		public Object getObject() {
			return object;
		}

		public String toString() {
			return "StructureTreeNodeHull[path=" + getPath() + ";object=" + getObject() + "]";
		}
		/***********************************************************************************
		 * REQUIRED METHODS FOR INTERFACES
		 **********************************************************************************/
	}
}
