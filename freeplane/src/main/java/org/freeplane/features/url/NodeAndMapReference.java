package org.freeplane.features.url;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NodeAndMapReference {
	private static final Pattern FREEPLANE_MAP_WITH_NODE_ID = Pattern.compile("\\.mm#(ID_\\d+)$", Pattern.CASE_INSENSITIVE);
	final private String nodeReference;
	final private String mapReference;
	final private boolean hasFreeplaneFileExtension;
	public NodeAndMapReference(String nodeInMapReference) {
		final boolean hasFreeplaneFileExtension = nodeInMapReference.toLowerCase().endsWith(
				UrlManager.FREEPLANE_FILE_EXTENSION);
		String mapReference = nodeInMapReference;
		String nodeReference = null;
		if (! hasFreeplaneFileExtension) {
			final Matcher matcher = FREEPLANE_MAP_WITH_NODE_ID.matcher(nodeInMapReference);
			if(matcher.find()) {
				nodeReference = matcher.group(1);
				mapReference = nodeInMapReference.substring(0, matcher.start(1) - 1);
			}
		}
		this.mapReference = mapReference;
		this.nodeReference = nodeReference;
		this.hasFreeplaneFileExtension = mapReference.toLowerCase().endsWith(UrlManager.FREEPLANE_FILE_EXTENSION);
	}
	public boolean hasFreeplaneFileExtension() {
		return hasFreeplaneFileExtension;
	}
	public String getNodeReference() {
		return nodeReference;
	}
	public boolean hasNodeReference() {
		return nodeReference != null;
	}
	public String getMapReference() {
		return mapReference;
	}
}