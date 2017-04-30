package org.freeplane.features.mode;

import java.util.HashSet;
import java.util.Set;

import org.freeplane.core.extension.IExtension;

public class MapExtensions {
	static private Set<Class<? extends IExtension>> mapExtensionClasses = new HashSet<Class<? extends IExtension>>();

	public static boolean isMapExtension(final Class<? extends IExtension> clazz) {
		return mapExtensionClasses.contains(clazz);
	}
	public static void registerMapExtension(final Class<? extends IExtension> extensionClass){
		mapExtensionClasses.add(extensionClass);
	}
}
