package org.docear.plugin.pdfutilities.ui.conflict;

import java.io.IOException;

import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;

import de.intarsys.pdf.cos.COSRuntimeException;
import de.intarsys.pdf.parser.COSLoadException;

public class SolveConflictForPdf implements ISolveConflictCommand {
	
	private AnnotationModel target;
	private String newTitle;
	
	public SolveConflictForPdf(AnnotationModel target, String newTitle) {
		super();
		this.setTarget(target);
		this.setNewTitle(newTitle);
	}

	public void solveConflict() {
		try {
			new PdfAnnotationImporter().renameAnnotation(getTarget(), getNewTitle());
		} catch (COSRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (COSLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public AnnotationModel getTarget() {
		return target;
	}

	public void setTarget(AnnotationModel target) {
		this.target = target;
	}

	public String getNewTitle() {
		return newTitle;
	}

	public void setNewTitle(String newTitle) {
		this.newTitle = newTitle;
	}

}
