package org.freeplane.core.ui.menubuilders.generic;

public interface Processor {

	void build(Entry entry);

	Processor forChildren(Entry root, Entry entry);

	void destroy(Entry entry);
}
