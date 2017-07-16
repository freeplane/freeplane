package org.freeplane.plugin.collaboration.client;

class UpdateBean {
	enum ContentType {
		CHILDREN, CLONES, TEXT, NOTE, DETAILS, ATTRIBUTES, ICONS, OTHER_CONTENT
	}
	
	private ContentType contentType;
	private String contentId;
	private String content;
	
	public ContentType getContentType() {
		return contentType;
	}
	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}
	public String getContentId() {
		return contentId;
	}
	public void setContentId(String contentId) {
		this.contentId = contentId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	@Override
	public String toString() {
		return "UpdateBean [contentType=" + contentType + ", contentId=" + contentId + ", content=" + content + "]";
	}
	
	
	
}
