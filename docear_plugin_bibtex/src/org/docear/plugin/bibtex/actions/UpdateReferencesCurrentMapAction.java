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

import org.docear.plugin.bibtex.JabRefAttributes;
import org.docear.plugin.bibtex.ReferencesController;
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
			private int totalNodeCount;
			private int count = 0;

			@Override
			protected Void doInBackground() throws Exception {
				//fireStatusUpdate(SwingWorkerDialog.SET_SUB_HEADLINE, null, "Updating References in progress....");
				fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
				
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Parsing bibtex files ...");
				if(canceled()) return null;
				database = ReferencesController.getController().getJabrefWrapper().getDatabase();
				if(canceled()) return null;
				if(database == null) return null;
				//uriMap = getUriMap(database);
				if(canceled()) return null;
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, "Computing total node count...");
				setTotalNodeCount(maps);
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
			
			private void setTotalNodeCount(List<MapModel> maps) {
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
				JabRefAttributes jabrefAttributes = ReferencesController.getController().getJabRefAttributes();
				String bibtexKey = jabrefAttributes.getBibtexKey(node);
				
				if (bibtexKey != null) {
					BibtexEntry entry = ReferencesController.getController().getJabrefWrapper().getDatabase().getEntryByKey(bibtexKey);
					
					if (entry != null) {
						jabrefAttributes.setReferenceToNode(entry, node);
					}
				}
				
				if(canceled()) return;
				count++;
				fireProgressUpdate(100 * count / totalNodeCount);
				
				for(NodeModel child : node.getChildren()){
					updateBibtexEntries(child);
				}
			}

		};
	}
}
