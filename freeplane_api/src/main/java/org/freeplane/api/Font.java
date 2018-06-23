package org.freeplane.api;

/** Node's font: <code>node.style.font</code> - read-write. */
public interface Font extends FontRO {
	void resetBold();

	void resetItalic();

	void resetName();

	void resetSize();

	void setBold(boolean bold);

	void setItalic(boolean italic);

	void setName(String name);

	void setSize(int size);
}