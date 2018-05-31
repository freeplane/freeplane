package org.freeplane.features.map;

public interface NodeChangeAnnouncer {
	@Deprecated
	public void nodeChanged(final NodeModel node);

	public void nodeChanged(final NodeModel node, final Object property, final Object oldValue, final Object newValue);

	@Deprecated
	public void nodeRefresh(final NodeModel node);

	public void nodeRefresh(final NodeModel node, final Object property, final Object oldValue, final Object newValue);

	public void nodeRefresh(final NodeChangeEvent nodeChangeEvent);
}
