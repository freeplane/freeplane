package org.freeplane.plugin.collaboration.client;

class UpdateBean {
	enum ContentType {
		CHILDREN, CLONES, TEXT, NOTE, DETAILS, ATTRIBUTES, ICONS, OTHER_CONTENT
	}
	
	private String nodeId;
	private ContentType contentType;
	private String content;
	
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
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
		return "UpdateBean [nodeIds" + nodeId + ", contentType=" + contentType + ", content=" + content + "]";
	}
	
	
	
}
