package org.freeplane.features.attribute;

import org.freeplane.core.extension.IExtension;

public class FontSizeExtension implements IExtension {

	public final int fontSize;

	public FontSizeExtension(int size) {
		this.fontSize = size;
	}

}
