package org.docear.plugin.core.features;

import java.net.URI;

import org.freeplane.core.extension.IExtension;

public interface IAnnotation extends IExtension{
	
	public enum AnnotationType{
		BOOKMARK, COMMENT, HIGHLIGHTED_TEXT, BOOKMARK_WITHOUT_DESTINATION, BOOKMARK_WITH_URI, PDF_FILE, FILE
	};
	
	public AnnotationID getAnnotationID();
	
	public void setAnnotationID(AnnotationID id);

	public AnnotationType getAnnotationType();

	public void setAnnotationType(AnnotationType annotationType);

	public Integer getPage();

	public void setPage(Integer page);

	public Integer getObjectNumber();

	public void setObjectNumber(Integer objectNumber);

	public Integer getGenerationNumber();

	public void setGenerationNumber(Integer generationNumber);

	public String getTitle();	

	public void setTitle(String title);	

	public URI getDestinationUri();

	public void setDestinationUri(URI uri);	
	
	public URI getUri();

	public boolean isNew();	

	public boolean hasNewChildren();

	public void setConflicted(boolean isConflicted);

	public boolean isConflicted();	

}