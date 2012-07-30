package org.docear.plugin.pdfutilities.listener;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.features.IAnnotation;
import org.docear.plugin.core.features.IAnnotation.AnnotationType;
import org.docear.plugin.core.logger.DocearLogEvent;
import org.docear.plugin.core.util.CoreUtils;
import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.view.swing.map.MainView;

import de.intarsys.pdf.parser.COSLoadException;

public class DocearNodeMouseMotionListener implements IMouseListener {

	private IMouseListener mouseListener;	
	private boolean wasFocused;	

	public DocearNodeMouseMotionListener(IMouseListener mouseListener) {
		this.mouseListener = mouseListener;	
	}	

	public void mouseDragged(MouseEvent e) {
		this.mouseListener.mouseDragged(e);
	}

	public void mouseMoved(MouseEvent e) {
		this.mouseListener.mouseMoved(e);
	}

	public void openPageMacOs(MouseEvent e, NodeModel node) {
		final String readerPath = ResourceController.getResourceController().getProperty(PdfUtilitiesController.OPEN_ON_PAGE_READER_COMMAND_KEY);
		if (readerPath == null || readerPath.length() == 0) {
			this.mouseListener.mouseClicked(e);
			return;
		}
		
		URI uri = Tools.getAbsoluteUri(node);
		if (!CoreUtils.resolveURI(uri).getName().toLowerCase().endsWith(".pdf")) {
			this.mouseListener.mouseClicked(e);
			return;
		}
		

		IAnnotation annotation = null;
		try {
			annotation = new PdfAnnotationImporter().searchAnnotation(uri, node);
			//System.gc();
			if (annotation == null || annotation.getPage() == null) {
				//Controller.exec(getExecCommandMacOs(readerPath, uri, 1));
				runAppleScript(readerPath, uri, 1);
				return;
			}
			else {
				runAppleScript(readerPath, uri, annotation.getPage());
				//Controller.exec(getExecCommandMacOs(readerPath, uri, annotation));
				return;
			}

		}		
		catch (COSLoadException x) {
			UITools.errorMessage("Could not find page because the document\n" + uri.toString() + "\nthrew a COSLoadExcpetion.\nTry to open file with standard options."); //$NON-NLS-1$ //$NON-NLS-2$
			System.err.println("Caught: " + x); //$NON-NLS-1$
		}
		catch (Exception x) {
			LogUtils.warn(x);
			this.mouseListener.mouseClicked(e);
			return;
		}
		
	}

	private void runAppleScript(String readerPath, URI fileUri, int page) throws ScriptException, URISyntaxException, IOException {	    	    	
    	File reader = new File(readerPath);
    	StringBuilder builder = new StringBuilder();
    	builder.append("global pdfPath\n");
    	builder.append("global page\n");
    	builder.append("set pdfPath to POSIX file \""+Tools.getFilefromUri(fileUri).getAbsolutePath()+"\"\n");
    	builder.append("set page to "+ page +" as text\n");
    	if(reader.exists() && reader.getAbsolutePath().endsWith(".app")){
    		builder.append("set pdfReaderPath to \""+reader.getAbsolutePath()+"\"\n\n");
    	}
    	else{
    		builder.append("set pdfReaderPath to null\n\n");
    	}
    	
    	URL url = PdfUtilitiesController.class.getResource("OpenOnPageScript.txt");
    	if (url != null) {    		
            InputStream input = url.openStream();
    		try{	    		
	            BufferedReader inStream = new BufferedReader(new InputStreamReader(input));
	            String inputLine;
	
	            while ((inputLine = inStream.readLine()) != null) {
	            	builder.append(inputLine + "\n");
	            }
    		}
    		finally{
    			input.close();
    		}
        }
    	else{
    		throw new IOException("Could not read applescript file.");
    	}
    	final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(null);
       
        ScriptEngineManager mgr = new ScriptEngineManager();
    	ScriptEngine engine = mgr.getEngineByName("AppleScript");
    	
        Thread.currentThread().setContextClassLoader(contextClassLoader);    	
		engine.eval(builder.toString());		
		LogUtils.info("Successfully ran apple script");
	}

	public void mouseClicked(MouseEvent e) {		
		boolean openOnPage = ResourceController.getResourceController().getBooleanProperty(
				PdfUtilitiesController.OPEN_PDF_VIEWER_ON_PAGE_KEY);		
		
		if (!openOnPage) {
			this.mouseListener.mouseClicked(e);
			return;
		}

		if (/*wasFocused() && */(e.getModifiers() & ~(InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK)) == InputEvent.BUTTON1_MASK) {
			final MainView component = (MainView) e.getComponent();
			final ModeController modeController = Controller.getCurrentModeController();
			NodeModel node = null;
			try {
				node = ((MainView) e.getSource()).getNodeView().getModel();
			}
			catch (Exception ex) {			
			}
			
			if (node==null) {
				node = modeController.getMapController().getSelectedNode();
			}
			
			if (component.isInFollowLinkRegion(e.getX())) {
				writeToLog(node);
			}
			if (!component.isInFollowLinkRegion(e.getX()) || !NodeUtils.isPdfLinkedNode(node)) {				
				this.mouseListener.mouseClicked(e);
				return;
			}
			
			if (openOnPage && Compat.isMacOsX()) {
				openPageMacOs(e, node);
				return;
			}
			
//			if (!Compat.isWindowsOS()) {
//				this.mouseListener.mouseClicked(e);
//				return;
//			}

			URI uri = Tools.getAbsoluteUri(node);

			String[] command = null;

			IAnnotation annotation = null;
			try {
				if(openOnPage){
					annotation = new PdfAnnotationImporter().searchAnnotation(uri, node);
					//System.gc();
					if (annotation == null) {
						if(uri == null) { 
							this.mouseListener.mouseClicked(e);
							return;
						}
						else {
							command = PdfUtilitiesController.getController().getPdfReaderExecCommand(uri, 1);
						}
					}
					else {
						if (annotation.getAnnotationType() == AnnotationType.BOOKMARK
								|| annotation.getAnnotationType() == AnnotationType.COMMENT
								|| annotation.getAnnotationType() == AnnotationType.HIGHLIGHTED_TEXT) {		
															
							command = PdfUtilitiesController.getController().getPdfReaderExecCommand(uri, annotation);
						}		
						if (annotation.getAnnotationType() == AnnotationType.BOOKMARK_WITHOUT_DESTINATION
								|| annotation.getAnnotationType() == AnnotationType.BOOKMARK_WITH_URI) {							
								command = PdfUtilitiesController.getController().getPdfReaderExecCommand(uri, 1);								
						}							
					}					
				}				
			}						
			catch (COSLoadException x) {
				UITools.errorMessage("Could not find page because the document\n" + uri.toString() + "\nthrew a COSLoadExcpetion.\nTry to open file with standard options."); //$NON-NLS-1$ //$NON-NLS-2$
				System.err.println("Caught: " + x); //$NON-NLS-1$
			}
			catch (Exception x) {
				this.mouseListener.mouseClicked(e);
				return;
			}

			LinkController.getController().onDeselect(node);

			// TODO: DOCEAR Are all URI's working ??
			/*
			 * String uriString = uri.toString(); final String UNC_PREFIX =
			 * "file:////"; if (uriString.startsWith(UNC_PREFIX)) { uriString =
			 * "file://" + uriString.substring(UNC_PREFIX.length()); }
			 */

			try {
				Controller.exec(command);
				//Controller.exec(command);
				return;
			}
			catch (final Exception x) {
				UITools.errorMessage("Could not invoke Pdf Reader.\n\nDocear excecuted the following statement on a command line:\n\"" + command + "\"."); //$NON-NLS-1$ //$NON-NLS-2$
				System.err.println("Caught: " + x); //$NON-NLS-1$
			}

			LinkController.getController().onSelect(node);
		}
		else {
			this.mouseListener.mouseClicked(e);
		}
	}
	
	

	private void writeToLog(NodeModel node) {
		URI uri = Tools.getAbsoluteUri(node);
		if(uri == null) {
			return;
		}
		if ("file".equals(uri.getScheme())) {
			File f = WorkspaceUtils.resolveURI(uri);
			//if map file is opened, then there is a MapLifeCycleListener Event
			if (f != null && !f.getName().endsWith(".mm")) {
				DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.FILE_OPENED,  f);
			}
		}
		else {					
			try {
				DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.OPEN_URL, uri.toURL());
			} catch (MalformedURLException ex) {						
				LogUtils.warn(ex);
			}
		}
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
