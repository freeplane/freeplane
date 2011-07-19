package org.freeplane.plugin.workspace.config;

import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.n3.nanoxml.XMLElement;

public abstract class NodeCreator implements IElementDOMHandler {
	
	private static class Path {
		static Path emptyPath() {
			final Path Path = new Path(null);
			Path.path = null;
			return Path;
		}

		String parentPath;
		String path;

		Path(final String path) {
			parentPath = path;
		}

		void setName(final String name) {
			path = parentPath == null ? name : parentPath + '/' + name;
		}

		@Override
		public String toString() {
			return path;
		}
	};	
	
	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		if (attributes == null) {
			return null;
		}
		final String name = attributes.getAttribute("name", null);
		if (name == null) {
			return parent == null ? Path.emptyPath() : parent;
		}
		final Path path = new Path(parent == null ? null : parent.toString());
		path.setName(name);
//		if (!tree.contains(path.path)) {
//			tree.addElement(path.parentPath == null ? tree : path.parentPath, this, path.path, IndexedTree.AS_CHILD);
//		}
		return path;
	}

	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement lastBuiltElement) {
		final String name = lastBuiltElement.getAttribute("name", null);
		final Path path = (Path)userObject;
		if (path.path == null) {
			return;
		}
//		final DefaultMutableTreeNode treeNode = tree.get(path.path);
//		if (treeNode.getUserObject() == this) {
//			final IPropertyControlCreator creator = getCreator(name, lastBuiltElement);
//			final String text = lastBuiltElement.getAttribute("text", null);
//			if(text == null){
//				treeNode.setUserObject(creator);
//			}
//			else{
//				treeNode.setUserObject(new IPropertyControlCreator(){
//					public IPropertyControl createControl() {
//						final IPropertyControl control = creator.createControl();
//						if( control instanceof PropertyAdapter){
//							final PropertyAdapter control2 = (PropertyAdapter) control;
//							control2.setLabel(text);
//						}
//						return control;
//                    }});
//			}
//			
//		}
	}

	abstract public DefaultMutableTreeNode getNode(String name, XMLElement data);
}