package org.freeplane.features.styles;

import org.freeplane.core.resources.NamedObject;

public class StyleFactory {
	public static IStyle create(String string){
		return new StyleString(string);
	}

	public static IStyle create(NamedObject no){
		return new StyleNamedObject(no);
	}
}
