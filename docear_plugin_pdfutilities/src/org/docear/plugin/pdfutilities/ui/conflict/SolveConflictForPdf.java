package org.docear.plugin.pdfutilities.ui.conflict;

import java.io.IOException;

import javax.swing.JOptionPane;

import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

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
		} catch (IOException e) {
			if(e.getMessage().equals("destination is read only")){ //$NON-NLS-1$
				int result = UITools.showConfirmDialog(null, TextUtils.getText("SolveConflictForPdf.1"), TextUtils.getText("SolveConflictForPdf.2"), JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
				if( result == JOptionPane.OK_OPTION){
					this.solveConflict();
				}
				else{
					//Controller.getCurrentModeController().rollback();
				}
			}
			else{
				LogUtils.severe("SolveConflictForPdf IOException at Target("+target.getTitle()+"): ", e); //$NON-NLS-1$ //$NON-NLS-2$
			}			
		} catch (COSLoadException e) {
			LogUtils.severe("SolveConflictForPdf COSLoadException at Target("+target.getTitle()+"): ", e); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (COSRuntimeException e) {
			LogUtils.severe("SolveConflictForPdf COSRuntimeException at Target("+target.getTitle()+"): ", e); //$NON-NLS-1$ //$NON-NLS-2$
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
