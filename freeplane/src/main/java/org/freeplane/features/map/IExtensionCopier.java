package org.freeplane.features.map;


public interface IExtensionCopier {
	void copy(Object key, NodeModel from, NodeModel to);

	void remove(Object key, NodeModel from);

	void remove(Object key, NodeModel from, NodeModel which);
	
	default void resolveParentExtensions(Object key, NodeModel to) {}
}
