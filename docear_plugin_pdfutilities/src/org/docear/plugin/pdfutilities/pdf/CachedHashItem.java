package org.docear.plugin.pdfutilities.pdf;


public class CachedHashItem {
	private String hashCode;
	private long lastUpdate;
	
	public CachedHashItem(String hash, long l) {
		this.hashCode = hash;
		this.lastUpdate = l;
	}	
	
	public String getHashCode() {
		return hashCode;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}
	
}