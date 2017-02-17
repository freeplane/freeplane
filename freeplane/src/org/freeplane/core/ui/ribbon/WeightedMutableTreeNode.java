package org.freeplane.core.ui.ribbon;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.tree.TreeNode;

public class WeightedMutableTreeNode<T> implements Cloneable, TreeNode, Serializable {

	private static final long serialVersionUID = -9105945271097923126L;

	public static final int FIRST = Integer.MIN_VALUE+100;
	public static final int PREPEND = Integer.MIN_VALUE+100000;
	public static final int APPEND = Integer.MAX_VALUE-100000;
	public static final int LAST = Integer.MAX_VALUE-100;
	

	private transient Comparator<WeightedMutableTreeNode<T>> comparator = new Comparator<WeightedMutableTreeNode<T>>() {
		public int compare(WeightedMutableTreeNode<T> n1, WeightedMutableTreeNode<T> n2) {
			if(n1.getWeight() > n2.getWeight()) {
				return 1;
			}
			else if(n1.getWeight() < n2.getWeight()) {
				return -1;
			} 
			return 0;
		}
	};

	public final Enumeration<WeightedMutableTreeNode<T>> EMPTY_ENUMERATION = new Enumeration<WeightedMutableTreeNode<T>>() {
		public boolean hasMoreElements() {
			return false;
		}

		public WeightedMutableTreeNode<T> nextElement() {
			throw new NoSuchElementException("No more elements");
		}
	};

	private Vector<WeightedMutableTreeNode<T>> children;

	protected transient T userObject;

	protected WeightedMutableTreeNode<T> parent;

	private final int weight;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public WeightedMutableTreeNode() {
		this(null);
	}

	public WeightedMutableTreeNode(T userObject) {
		this(userObject, APPEND);
	}

	public WeightedMutableTreeNode(int weight) {
		this(null, weight);
	}

	public WeightedMutableTreeNode(T userObject, int weight) {
		this.userObject = userObject;
		this.weight = weight;
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public void addChild(WeightedMutableTreeNode<T> newChild) {
		if (newChild == null) {
			throw new IllegalArgumentException("new child is null");
		} else if (isNodeAncestor(newChild)) {
			throw new IllegalArgumentException("new child is an ancestor");
		}

		WeightedMutableTreeNode<T> oldParent = newChild.getParent();

		if (oldParent != null) {
			oldParent.remove(newChild);
		}
		newChild.setParent(this);

		if (children == null) {
			children = new Vector<WeightedMutableTreeNode<T>>();
		}
		children.add(newChild);

		Collections.sort(children, comparator);
	}

	public void remove(int childIndex) {
		WeightedMutableTreeNode<T> child = getChildAt(childIndex);
		children.removeElementAt(childIndex);
		child.setParent(null);
	}

	public void remove(WeightedMutableTreeNode<T> aChild) {
		if (aChild == null) {
			throw new IllegalArgumentException("argument is null");
		}

		if (!isNodeChild(aChild)) {
			throw new IllegalArgumentException("argument is not a child");
		}
		remove(getIndex(aChild));
	}

	public void removeAllChildren() {
		for (int i = getChildCount() - 1; i >= 0; i--) {
			remove(i);
		}
	}

	void setParent(WeightedMutableTreeNode<T> parent) {
		this.parent = parent;
	}

	public int getWeight() {
		return weight;
	}

	public boolean isNodeAncestor(WeightedMutableTreeNode<T> anotherNode) {
		if (anotherNode == null) {
			return false;
		}

		TreeNode ancestor = this;

		do {
			if (ancestor == anotherNode) {
				return true;
			}
		} while ((ancestor = ancestor.getParent()) != null);

		return false;
	}

	public boolean isNodeDescendant(WeightedMutableTreeNode<T> anotherNode) {
		if (anotherNode == null) {
			return false;
		}

		return anotherNode.isNodeAncestor(this);
	}

	public boolean isNodeChild(TreeNode aNode) {
		boolean retval;

		if (aNode == null) {
			retval = false;
		} else {
			if (getChildCount() == 0) {
				retval = false;
			} else {
				retval = (aNode.getParent() == this);
			}
		}

		return retval;
	}
	
	public T getUserObject() {
		return this.userObject;
	}
	
	public void setUserObject(T o) {
		this.userObject = o;
	}
	
	

	@Override
	public String toString() {
		return "WeightedMutableTreeNode [userObject=" + userObject
				+ ", weight=" + weight + "]";
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public WeightedMutableTreeNode<T> getChildAt(int childIndex) {
		if (children == null) {
			throw new ArrayIndexOutOfBoundsException("node has no children");
		}
		return children.get(childIndex);
	}

	public int getChildCount() {
		return children == null ? 0 : children.size();
	}

	public WeightedMutableTreeNode<T> getParent() {
		return parent;
	}

	public int getIndex(TreeNode aChild) {
		if (aChild == null) {
			throw new IllegalArgumentException("argument is null");
		}

		if (!isNodeChild(aChild)) {
			return -1;
		}
		return children.indexOf(aChild);
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public boolean isLeaf() {
		return (getChildCount() == 0);
	}

	public Enumeration<WeightedMutableTreeNode<T>> children() {
		if (children == null) {
			return EMPTY_ENUMERATION;
		} else {
			return children.elements();
		}
	}

	/***********************************************************************************
	 * NESTED TYPE DECLARATION
	 **********************************************************************************/
}
