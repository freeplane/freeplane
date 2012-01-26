package org.docear.plugin.pdfutilities.listener;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.pdf.PdfReaderFileFilter;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.features.link.LinkController;
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
		boolean openOnPageWine = ResourceController.getResourceController().getBooleanProperty(PdfUtilitiesController.OPEN_PDF_VIEWER_ON_PAGE_KEY_WINE);
		String readerPath = ResourceController.getResourceController().getProperty(PdfUtilitiesController.OPEN_ON_PAGE_READER_PATH_KEY);
		String readerPathWine = ResourceController.getResourceController().getProperty(PdfUtilitiesController.OPEN_ON_PAGE_READER_PATH_KEY_WINE);
		
		if((!openOnPage || !isValidReaderPath(readerPath)) && (!openOnPageWine || readerPathWine==null)) {
			this.mouseListener.mouseClicked(e);
			return;
		}
		
		if (wasFocused() && (e.getModifiers() & ~ (InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK)) == InputEvent.BUTTON1_MASK) {
			final MainView component = (MainView) e.getComponent();
			final ModeController modeController = Controller.getCurrentModeController();
			final NodeModel selectedNode = modeController.getMapController().getSelectedNode();
			final String osName = System.getProperty("os.name"); //$NON-NLS-1$
			
			if (!component.isInFollowLinkRegion(e.getX()) ||
				!NodeUtils.isPdfLinkedNode(selectedNode) ||
				(!osName.substring(0, 3).equals("Win") && !openOnPageWine)) { //$NON-NLS-1$
				this.mouseListener.mouseClicked(e);
				return;
			}
			
			URI uri = Tools.getAbsoluteUri(selectedNode);				
			
			String[] command = null;
			
			IAnnotation annotation = null;
			try{
				annotation = new PdfAnnotationImporter().searchAnnotation(uri, selectedNode);
				
				if(annotation == null){
					this.mouseListener.mouseClicked(e);
					return;
				}
				
				if(annotation.getAnnotationType() == AnnotationType.BOOKMARK ||
						annotation.getAnnotationType() == AnnotationType.COMMENT ||
						annotation.getAnnotationType() == AnnotationType.HIGHLIGHTED_TEXT){
					if(annotation.getPage() != null) {
						if (openOnPage) {
							command = getExecCommand(readerPath, uri, annotation);
						}
						else if (openOnPageWine) {
							command = getExecCommandWine(readerPathWine, uri, annotation);
						}
					}
					if(annotation.getPage() == null){
						//TODO: DOCEAR Error Message for User ??
						this.mouseListener.mouseClicked(e);							
						return;
					}
				}
				
				if(annotation.getAnnotationType() == AnnotationType.BOOKMARK_WITHOUT_DESTINATION ||
						annotation.getAnnotationType() == AnnotationType.BOOKMARK_WITH_URI){
					if (openOnPage) {
						command = getExecCommand(readerPath, uri, 1);
					}
					else if (openOnPageWine) {
						command = getExecCommandWine(readerPath, uri, 1);
					}
				}
				
				
			}catch(IOException x){
				this.mouseListener.mouseClicked(e);
				return;
			}catch(COSLoadException x){
				UITools.errorMessage("Could not find page because the document\n" + uri.toString() + "\nthrew a COSLoadExcpetion.\nTry to open file with standard options."); //$NON-NLS-1$ //$NON-NLS-2$
				System.err.println("Caught: " + x); //$NON-NLS-1$
			}
			
			LinkController.getController().onDeselect(selectedNode);
			
			
			//TODO: DOCEAR Are all URI's working ??
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
				UITools.errorMessage("Could not invoke Pdf Reader.\n\nDocear excecuted the following statement on a command line:\n\"" + command + "\"."); //$NON-NLS-1$ //$NON-NLS-2$
				System.err.println("Caught: " + x); //$NON-NLS-1$
			}
				
			LinkController.getController().onSelect(selectedNode);			
		}
		else{
			this.mouseListener.mouseClicked(e);
		}
	}

	private String[] getExecCommand(String readerPath, URI uriToFile, int page) {
		PdfReaderFileFilter readerFilter = new PdfReaderFileFilter();
		File file = Tools.getFilefromUri(Tools.getAbsoluteUri(uriToFile, Controller.getCurrentController().getMap()));
		String[] command = new String[4];
		if(readerFilter.isAdobe(new File(readerPath)) && file != null){
			command[0] = readerPath;
			command[1] = "/A";
			command[2] = "page=" + page;
			command[3] = file.getAbsolutePath();
			return  command;  //$NON-NLS-1$ //$NON-NLS-2$
		}
		if(readerFilter.isFoxit(new File(readerPath)) && file != null){
			command[0] = readerPath;
			command[1] = file.getAbsolutePath();
			command[2] = "/A";
			command[3] = "page=" + page;
			return command; //$NON-NLS-1$ //$NON-NLS-2$
		}
		if(readerFilter.isPdfXChange(new File(readerPath)) && file != null){
			command[0] = readerPath;
			command[1] = "/A";
			command[2] = "page=" + page;
			command[3] = file.getAbsolutePath();
			return command; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return null;
	}
	
	private String[] getExecCommand(String readerPath, URI uriToFile, IAnnotation annotation) {
		PdfReaderFileFilter readerFilter = new PdfReaderFileFilter();
		File file = Tools.getFilefromUri(Tools.getAbsoluteUri(uriToFile, Controller.getCurrentController().getMap()));
		String[] command = new String[4];
		if(readerFilter.isAdobe(new File(readerPath)) && file != null){
			command[0] = readerPath;
			command[1] = "/A";
			command[2] = "page=" + annotation.getPage();
			command[3] = file.getAbsolutePath();
			return  command;  //$NON-NLS-1$ //$NON-NLS-2$
		}
		if(readerFilter.isFoxit(new File(readerPath)) && file != null){
			command[0] = readerPath;
			command[1] = file.getAbsolutePath();
			command[2] = "/A";
			command[3] = "page=" + annotation.getPage();
			return command; //$NON-NLS-1$ //$NON-NLS-2$
		}
		if(readerFilter.isPdfXChange(new File(readerPath)) && file != null){
			command[0] = readerPath;
			command[1] = "/A";
			command[2] = "page=" + annotation.getPage() + "&nameddest=" + annotation.getTitle();
			command[3] = file.getAbsolutePath();
			return command;
		}
		return null;
	}
	
	private String[] getExecCommandWine(String readerPathWine, URI uriToFile, IAnnotation annotation) {
		return getExecCommandWine(readerPathWine, uriToFile, annotation.getPage());
	}
	
	private String[] getExecCommandWine(String readerPathWine, URI uriToFile, int page) {
		String wineFile = Tools.getFilefromUri(uriToFile).getAbsolutePath();		
		wineFile = "Z:"+wineFile.replace("/", "\\")+"";
		
		String[] command = new String[6];
		command[0] = "bash";
		command[1] = "wine";
		command[2] = readerPathWine;
		command[3] = wineFile;
		command[4] = "/A";
		command[5] = "page=" + page;		
		
		return  command;		
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
