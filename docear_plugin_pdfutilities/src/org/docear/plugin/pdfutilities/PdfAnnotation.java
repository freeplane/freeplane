package org.docear.plugin.pdfutilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PdfAnnotation {
	
	public static final int BOOKMARK = 0;
	public static final int COMMENT = 1;
	public static final int HIGHLIGHTED_TEXT = 2;
	
	
	private File file;
	private String title;
	private List<PdfAnnotation> children = new ArrayList<PdfAnnotation>();
	private int annotationType;
	
	public PdfAnnotation(){}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<PdfAnnotation> getChildren() {
		return children;
	}	

	public int getAnnotationType() {
		return annotationType;
	}

	public void setAnnotationType(int annotationType) {
		this.annotationType = annotationType;
	}
	

}
