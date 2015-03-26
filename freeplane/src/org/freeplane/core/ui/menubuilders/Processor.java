package org.freeplane.core.ui.menubuilders;

public interface Processor {

	void build(Entry entry);

	Processor forChildren(Entry root, Entry entry);

}
