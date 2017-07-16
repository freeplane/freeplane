package org.freeplane.plugin.collaboration.client;

import java.util.List;

class UpdateBean {
	enum ContentType {
		CHILDREN, CLONES, TEXT, NOTE, DETAILS, ATTRIBUTES, ICONS, OTHER_CONTENT
	}
	
	private List<String> nodeIds;
	private ContentType contentType;
	public List<String> getNodeIds() {
		return nodeIds;
	}
	public void setNodeIds(List<String> nodeIds) {
		this.nodeIds = nodeIds;
	}
	private String content;
	
	public ContentType getContentType() {
		return contentType;
	}
	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	@Override
	public String toString() {
		return "UpdateBean [nodeIds=" + nodeIds + ", contentType=" + contentType + ", content=" + content + "]";
	}
	
	
	
}
