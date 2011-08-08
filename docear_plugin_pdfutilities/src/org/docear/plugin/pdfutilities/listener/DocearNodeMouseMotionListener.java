package org.docear.plugin.pdfutilities.listener;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.features.PdfAnnotationExtensionModel.AnnotationType;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotation;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.pdf.PdfReaderFileFilter;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.docear.plugin.pdfutilities.util.Tools;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.view.swing.map.MainView;

import de.intarsys.pdf.parser.COSLoadException;

public class DocearNodeMouseMotionListener implements IMouseListener {
	
	private IMouseListener mouseListener;
	private boolean wasFocused;
	
	public DocearNodeMouseMotionListener(IMouseListener mouseListener){
		this.mouseListener = mouseListener;
	}

	public void mouseDragged(MouseEvent e) {
		this.mouseListener.mouseDragged(e);
	}

	public void mouseMoved(MouseEvent e) {
		this.mouseListener.mouseMoved(e);
	}

	public void mouseClicked(MouseEvent e) {
		boolean openOnPage = ResourceController.getResourceController().getBooleanProperty(PdfUtilitiesController.OPEN_PDF_VIEWER_ON_PAGE_KEY);
		String readerPath = ResourceController.getResourceController().getProperty(PdfUtilitiesController.OPEN_ON_PAGE_READER_PATH_KEY);
		
		if(!openOnPage || !isValidReaderPath(readerPath)){
			this.mouseListener.mouseClicked(e);
			return;
		}
		
		if (wasFocused() && (e.getModifiers() & ~ (InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK)) == InputEvent.BUTTON1_MASK) {
			final MainView component = (MainView) e.getComponent();
			final ModeController modeController = Controller.getCurrentModeController();
			final NodeModel selectedNode = modeController.getMapController().getSelectedNode();
			final String osName = System.getProperty("os.name");
			
			if (!component.isInFollowLinkRegion(e.getX()) ||
				!NodeUtils.isPdfLinkedNode(selectedNode) ||
				!osName.substring(0, 3).equals("Win")) {
				this.mouseListener.mouseClicked(e);
				return;
			}
			
			URI uri = NodeLinks.getValidLink(selectedNode);					
			uri = Tools.getAbsoluteUri(uri);
			String command = null;
			
			PdfAnnotation annotation = null;
			try{
				annotation = new PdfAnnotationImporter().searchAnnotation(Tools.getFilefromUri(uri), selectedNode.getText());
				
				if(annotation == null){
					this.mouseListener.mouseClicked(e);
					return;
				}
				
				if(annotation.getAnnotationType() == AnnotationType.BOOKMARK ||
						annotation.getAnnotationType() == AnnotationType.COMMENT ||
						annotation.getAnnotationType() == AnnotationType.HIGHLIGHTED_TEXT){
					if(annotation.getPage() != null){
						command = getExecCommand(readerPath, uri, annotation.getPage());						
					}
					if(annotation.getPage() == null){
						//TODO: DOCEAR Error Message for User ??
						this.mouseListener.mouseClicked(e);							
						return;
					}
				}
				
				if(annotation.getAnnotationType() == AnnotationType.BOOKMARK_WITHOUT_DESTINATION ||
						annotation.getAnnotationType() == AnnotationType.BOOKMARK_WITH_URI){
					command = getExecCommand(readerPath, uri, 1);					
				}
				
				
			}catch(IOException x){
				this.mouseListener.mouseClicked(e);
				return;
			}catch(COSLoadException x){
				UITools.errorMessage("Could not find page because the document\n" + uri.toString() + "\nthrew a COSLoadExcpetion.\nTry to open file with standard options.");
				System.err.println("Caught: " + x);
			}
			
			LinkController.getController().onDeselect(selectedNode);
			
			
			//TODO Are all URI's working ??
			/*String uriString = uri.toString();
			final String UNC_PREFIX = "file:////";
			if (uriString.startsWith(UNC_PREFIX)) {
				uriString = "file://" + uriString.substring(UNC_PREFIX.length());
			}*/			
				
			try {								
				Controller.exec(command);
				return;
			}					
			catch (final IOException x) {
				UITools.errorMessage("Could not invoke Pdf Reader.\n\nDocear excecuted the following statement on a command line:\n\"" + command + "\".");
				System.err.println("Caught: " + x);
			}
				
			LinkController.getController().onSelect(selectedNode);			
		}
		else{
			this.mouseListener.mouseClicked(e);
		}
	}

	private String getExecCommand(String readerPath, URI uriToFile, int page) {
		PdfReaderFileFilter readerFilter = new PdfReaderFileFilter();
		if(readerFilter.isAdobe(new File(readerPath))){
			return readerPath + " /A page=" + page + " " + Tools.getFilefromUri(uriToFile).getAbsolutePath();
		}
		if(readerFilter.isFoxit(new File(readerPath))){
			return readerPath + " \"" + Tools.getFilefromUri(uriToFile).getAbsolutePath() + "\" /A page=" + page;
		}
		return null;
	}

	private boolean isValidReaderPath(String readerPath) {		
		return readerPath != null && readerPath.length() > 0 && new File(readerPath).exists() && new PdfReaderFileFilter().accept(new File(readerPath));
	}

	public void mousePressed(MouseEvent e) {
		final MainView component = (MainView) e.getComponent();
		wasFocused = component.hasFocus();
		this.mouseListener.mousePressed(e);
	}

	public void mouseReleased(MouseEvent e) {
		this.mouseListener.mouseReleased(e);
	}

	public void mouseEntered(MouseEvent e) {
		this.mouseListener.mouseEntered(e);
	}

	public void mouseExited(MouseEvent e) {
		this.mouseListener.mouseExited(e);
	}
	
	public boolean wasFocused() {
    	return wasFocused;
    }

}
