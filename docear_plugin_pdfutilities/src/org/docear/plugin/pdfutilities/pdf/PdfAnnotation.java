package org.docear.plugin.pdfutilities.pdf;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.freeplane.features.map.NodeModel;

public class PdfAnnotation {
	
	public static final int BOOKMARK = 0;
	public static final int COMMENT = 1;
	public static final int HIGHLIGHTED_TEXT = 2;
	
	
	private File file;
	private String title;
	private List<PdfAnnotation> children = new ArrayList<PdfAnnotation>();
	private int annotationType;
	private boolean isNew;
	
	public PdfAnnotation(){}

	public URI getAbsoluteUri(){
		if(this.file != null){
			return this.file.getAbsoluteFile().toURI();
		}
		else{
			return null;
		}
	}
	
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
	
	private void setNew(boolean isNew){
		this.isNew = isNew;
	}
	
	public boolean isNew(){
		return this.isNew;
	}
	
	public boolean hasNewChildren(){
		for(PdfAnnotation child : this.children){
			if(child.isNew() || child.hasNewChildren()){
				return true;
			}
		}
		return false;
	}
	
	public void markNewAnnotations(Map<URI, Collection<NodeModel>> pdfLinkedNodes){
		for(PdfAnnotation child : this.getChildren()){
			child.markNewAnnotations(pdfLinkedNodes);
		}
		if(pdfLinkedNodes.containsKey(this.getAbsoluteUri())){
			for(NodeModel node : pdfLinkedNodes.get(this.getAbsoluteUri())){
				if(node.getText().equals(this.getTitle())){
					this.setNew(false);
					return;
				}
			}
		}
		this.setNew(true);
	}
	
	public static Collection<PdfAnnotation> markNewAnnotations(Collection<PdfAnnotation> annotations, 
										  Map<URI, Collection<NodeModel>> pdfLinkedNodes){
		for(PdfAnnotation annotation : annotations){
			annotation.markNewAnnotations(pdfLinkedNodes);
		}
		return annotations;
	}
	

}
