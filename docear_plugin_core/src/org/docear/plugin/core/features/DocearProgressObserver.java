package org.docear.plugin.core.features;

public interface DocearProgressObserver {

	void update(Object source, int progress, int length);

}
