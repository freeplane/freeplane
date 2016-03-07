package org.freeplane.features.styles;

import org.freeplane.core.resources.TranslatedObject;

public class StyleFactory {
	public static IStyle create(String string){
		return new StyleString(string);
	}

	public static IStyle create(TranslatedObject no){
		return new StyleTranslatedObject(no);
	}
}
