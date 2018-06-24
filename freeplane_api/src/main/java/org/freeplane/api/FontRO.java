package org.freeplane.api;

/** Node's font: <code>node.style.font</code> - read-only. */
public interface FontRO {
	String getName();

	int getSize();

	boolean isBold();

	boolean isBoldSet();

	boolean isItalic();

	boolean isItalicSet();

	boolean isNameSet();

	boolean isSizeSet();
}