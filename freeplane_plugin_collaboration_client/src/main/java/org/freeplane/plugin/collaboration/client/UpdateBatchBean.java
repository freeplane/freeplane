package org.freeplane.plugin.collaboration.client;

import java.util.List;

class UpdateBatchBean {
	private String mapId;
	private Long mapRevision;
	private List<UpdateBean> updates;

	public List<UpdateBean> getUpdates() {
		return updates;
	}

	public void setUpdates(List<UpdateBean> updates) {
		this.updates = updates;
	}

	public String getMapId() {
		return mapId;
	}

	public void setMapId(String mapId) {
		this.mapId = mapId;
	}

	public Long getMapRevision() {
		return mapRevision;
	}

	public void setMapRevision(Long mapRevision) {
		this.mapRevision = mapRevision;
	}

	@Override
	public String toString() {
		return "UpdateBatchBean [mapId=" + mapId + ", mapRevision=" + mapRevision + ", updates=" + updates + "]";
	}
	
}
