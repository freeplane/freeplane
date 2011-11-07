package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

import javax.swing.SwingUtilities;

import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.imports.OpenDatabaseAction;
import net.sf.jabref.imports.ParserResult;

import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.ui.SwingWorkerDialog;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.mindmapmode.SaveAll;
import org.jdesktop.swingworker.SwingWorker;

public class UpdateReferencesCurrentMapAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UpdateReferencesCurrentMapAction(String key) {
		super(key);		
	}

	public void actionPerformed(ActionEvent e) {
		new SaveAll().actionPerformed(null);
				
		try {	
			List<MapModel> maps = new ArrayList<MapModel>();
			maps.add(Controller.getCurrentController().getMap());
			SwingWorker<Void, Void> thread = getReferenceUpdateThread(maps);		
			
			SwingWorkerDialog workerDialog = new SwingWorkerDialog(Controller.getCurrentController().getViewController().getJFrame());
			workerDialog.setHeadlineText("Reference Update");
			workerDialog.setSubHeadlineText("Updating References in progress....");
			workerDialog.showDialog(thread);
			workerDialog = null;			
			
		} catch (CancellationException ex){
			LogUtils.info("CancellationException during reference update.");
		}
					
	}
	
	public static SwingWorker<Void, Void> getReferenceUpdateThread(final List<MapModel> maps){
		
		return new SwingWorker<Void, Void>(){
			private BibtexDatabase database;
			private Map<URI, BibtexEntry> uriMap;
			private int totalNodeCount;
			private int count = 0;

			@Override
			protected Void doInBackground() throws Exception {
				//fireStatusUpdate(SwingWorkerDialog.SET_SUB_HEADLINE, null, "Updating References in progress....");
				fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
				
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Parsing bibtex files ...");
				if(canceled()) return null;
				database = getBibtexDatabase();
				if(canceled()) return null;
				if(database == null) return null;
				uriMap = getUriMap(database);
				if(canceled()) return null;
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Computing total node count...");
				getTotalNodeCount(maps);
				if(canceled()) return null;
				fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
				fireProgressUpdate(100 * count / totalNodeCount);
				for(MapModel map : maps){
					fireStatusUpdate(SwingWorkerDialog.SET_SUB_HEADLINE, null, "Updating References against "+ map.getTitle() +" in progress....");
					updateBibtexEntries(map.getRootNode());
					if(canceled()) return null;
				}			
				return null;
			}
			
			@Override
		    protected void done() {			
				if(this.isCancelled() || Thread.currentThread().isInterrupted()){					
					this.firePropertyChange(SwingWorkerDialog.IS_DONE, null, "Reference update canceled.");
				}
				else{
					this.firePropertyChange(SwingWorkerDialog.IS_DONE, null, "Reference update complete.");
				}
				
			}
			
			private boolean canceled() throws InterruptedException{
				Thread.sleep(1L);
				return (this.isCancelled() || Thread.currentThread().isInterrupted());
			}
			
			private void fireStatusUpdate(final String propertyName, final Object oldValue, final Object newValue) throws InterruptedException, InvocationTargetException{				
				SwingUtilities.invokeAndWait(
				        new Runnable() {
				            public void run(){
				            	firePropertyChange(propertyName, oldValue, newValue);										
				            }
				        }
				   );	
			}
			
			private void fireProgressUpdate(final int progress) throws InterruptedException, InvocationTargetException{
				SwingUtilities.invokeAndWait(
				        new Runnable() {
				            public void run(){
				            	setProgress(progress);						
				            }
				        }
				   );	
			}
			
			private void getTotalNodeCount(List<MapModel> maps) {
				for(MapModel map : maps){
					getTotalNodeCount(map.getRootNode());
				}					
			}

			private void getTotalNodeCount(NodeModel node) {
				if(node.isRoot()){
					this.totalNodeCount++;
				}
				this.totalNodeCount += node.getChildCount();
				for(NodeModel child : node.getChildren()){
					getTotalNodeCount(child);
				}					
			}


			private void updateBibtexEntries(NodeModel node) throws InterruptedException, InvocationTargetException {
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Checking References for node: " + node.getText());
				URI uri = Tools.getAbsoluteUri(node);
				if(uri != null && uriMap.containsKey(uri)){
					setBibtexEntry(node, uriMap.get(uri));
				}
				if(canceled()) return;
				count++;
				fireProgressUpdate(100 * count / totalNodeCount);
				for(NodeModel child : node.getChildren()){
					updateBibtexEntries(child);
				}
			}

			private void setBibtexEntry(final NodeModel node, final BibtexEntry entry) throws InterruptedException, InvocationTargetException {
				fireStatusUpdate(SwingWorkerDialog.DETAILS_LOG_TEXT, null, "Updating References for node: " + node.getText());
				final String prefix = "bibtex_";
				SwingUtilities.invokeAndWait(new Runnable() {					
					
					public void run() {
						Tools.setAttributeValue(node, prefix + "key", entry.getCiteKey());
						Tools.setAttributeValue(node, prefix + "type", entry.getType().getName());
					}
				});
				if(canceled()) return;
				for(final String field : entry.getAllFields()){
					String temp = entry.getField(field);
					if(temp.length() <= 0) continue;
					final String value = parseSpecialChars(temp);
					SwingUtilities.invokeAndWait(new Runnable() {						
						
						public void run() {
							Tools.setAttributeValue(node, prefix + field, value);
						}
					});
				}
				if(canceled()) return;
				for(final String attributeKey : Tools.getAllAttributeKeys(node)){
					String temp = attributeKey.replace(prefix, "");
					if(!entry.getAllFields().contains(temp) || entry.getField(temp).length() <= 0){
						SwingUtilities.invokeAndWait(new Runnable() {						
							
							public void run() {
								Tools.removeAttributeValue(node, attributeKey);
							}
						});
					}
				}
			}

			
			
		};
	}
	
	public static Map<URI, BibtexEntry> getUriMap(BibtexDatabase database) {
		Map<URI, BibtexEntry> result = new HashMap<URI, BibtexEntry>();
		for(BibtexEntry entry : database.getEntries()){
			String files = entry.getField("file");
			if(files != null && files.length() > 0){
				String[] paths = files.split("(?<!\\\\);"); // taken from splmm, could not test it
                for(String path : paths){
                	ResourceController resourceController = ResourceController.getResourceController();
                	String source = resourceController.getProperty("docear_bibtex_source", "Jabref");
                	path = removeMendeleyBackSlash(path);
                	path = extractPath(path);
                	if(path == null){
                		LogUtils.warn("Could not extract path from: "+ entry.getCiteKey());
                		continue; 
                	}
                	if(source.equalsIgnoreCase("docear_bibtex_source.mendeley")){
                		path = parseSpecialChars(path); // Mendeley uses escaping constructs for specials characters
                		path = removeEscapingCharacter(path); 
                		if(new File(path).exists()){
            				result.put(new File(path).toURI(), entry);
            			}
                	}
                	if(source.equalsIgnoreCase("docear_bibtex_source.jabref")){
                		path = removeEscapingCharacter(path);
                		if(isAbsolutePath(path)){
                			if(new File(path).exists()){
                				result.put(new File(path).toURI(), entry);
                			}
                		}
                		else{
                			try {
    							URI uri = new URI("property:/" + CoreConfiguration.BIBTEX_PATH).resolve(path);
    							if(new File(uri.normalize()) != null && new File(uri.normalize()).exists()){
    								result.put(uri, entry);
    							}
    						} catch (URISyntaxException e) {
    							LogUtils.warn(e);
    							continue;
    						}
                		}
                	}
                	if(source.equalsIgnoreCase("docear_bibtex_source.zotero")){
                		try {
							URI uri = new URI("property:/" + CoreConfiguration.BIBTEX_PATH).resolve(path);
							if(new File(uri.normalize()) != null && new File(uri.normalize()).exists()){
								result.put(uri, entry);
							}
						} catch (URISyntaxException e) {
							LogUtils.warn(e);
							continue;
						}
                	}
                }
            }
		}		
		return result;
	}
	
	private static boolean isAbsolutePath(String path) {
		return path.matches("^/") || path.matches("^[a-zA-Z]:");
	}

	private static String removeEscapingCharacter(String string) {
		return string.replace("\\\\(?=[^\\])", "");
	}

	private static String extractPath(String path) {
		String[] array = path.split("(^:|(?<=[^\\\\]):)"); // splits the string at non escaped double points
		if(array.length >= 3){
			return array[1];
		}
		return null;
	}

	private static String removeMendeleyBackSlash(String path) {
        path = path.replace("$backslash$", "\\");       
        //path = path.replace('/', '\\');
        return path;
    }
	
	public static String parseSpecialChars(String s){
        if(s == null) return s;
        s = s.replaceAll("\\\\\"[{]([a-zA-Z])[}]",  "$1" + "\u0308"); // replace Ìˆ
        s = s.replaceAll("\\\\`[{]([a-zA-Z])[}]",  "$1" + "\u0300"); // replace `
        s = s.replaceAll("\\\\Â´[{]([a-zA-Z])[}]",  "$1" + "\u0301"); // replace Â´
        s = s.replaceAll("\\\\'[{]([a-zA-Z])[}]",  "$1" + "\u0301"); // replace Â´
        s = s.replaceAll("\\\\\\^[{]([a-zA-Z])[}]",  "$1" + "\u0302"); // replace ^
        s = s.replaceAll("\\\\~[{]([a-zA-Z])[}]",  "$1" + "\u0303"); // replace ~
        s = s.replaceAll("\\\\=[{]([a-zA-Z])[}]",  "$1" + "\u0304"); // replace - above
        s = s.replaceAll("\\\\\\.[{]([a-zA-Z])[}]",  "$1" + "\u0307"); // replace . above
        s = s.replaceAll("\\\\u[{]([a-zA-Z])[}]",  "$1" + "\u030c"); // replace v above
        s = s.replaceAll("\\\\v[{]([a-zA-Z])[}]",  "$1" + "\u0306"); // replace combining breve
        s = s.replaceAll("\\\\H[{]([a-zA-Z])[}]",  "$1" + "\u030b"); // replace double acute accent
        s = s.replaceAll("\\\\t[{]([a-zA-Z])([a-zA-Z])[}]",  "$1" + "\u0361" + "$2"); // replace double inverted breve
        s = s.replaceAll("\\\\c[{]([a-zA-Z])[}]",  "$1" + "\u0355"); // replace right arrowhead below
        s = s.replaceAll("\\\\d[{]([a-zA-Z])[}]",  "$1" + "\u0323"); // replace . below
        s = s.replaceAll("\\\\b[{]([a-zA-Z])[}]",  "$1" + "\u0331"); // replace - below

        if(s.contains("\\ss")){
            s = s.replace("\\ss", "\u00df");
        }
        if(s.contains("\\AE")){
            s = s.replace("\\AE", "\u00c6");
        }
        if(s.contains("\\ae")){
            s = s.replace("\\ae", "\u00e6");
        }
        if(s.contains("\\OE")){
            s = s.replace("\\OE", "\u0152");
        }
        if(s.contains("\\oe")){
            s = s.replace("\\oe", "\u0153");
        }
        if(s.contains("\\O")){
            s = s.replace("\\O", "\u00d8");
        }
        if(s.contains("\\o")){
            s = s.replace("\\o", "\u00f8");
        }
        if(s.contains("\\L")){
            s = s.replace("\\L", "\u0141");
        }
        if(s.contains("\\l")){
            s = s.replace("\\l", "\u0142");
        }
        if(s.contains("\\AA")){
            s = s.replace("\\AA", "\u00c5");
        }
        if(s.contains("\\aa")){
            s = s.replace("\\aa", "\u00e5");
        }
        return s;
    }

	public static BibtexDatabase getBibtexDatabase() {
		ResourceController resourceController = ResourceController.getResourceController();
		try {
			File bibtexFile = new File(resourceController.getProperty(CoreConfiguration.BIBTEX_PATH, ""));
		
			if(bibtexFile == null || !bibtexFile.exists()) return null;				
		
			ParserResult parserResult = OpenDatabaseAction.loadDatabase(bibtexFile, resourceController.getProperty("docear_bibtex_encoding", "UTF8"));
			return parserResult.getDatabase();					
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
