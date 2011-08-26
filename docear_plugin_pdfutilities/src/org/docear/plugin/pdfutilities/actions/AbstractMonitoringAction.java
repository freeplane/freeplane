package org.docear.plugin.pdfutilities.actions;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import org.docear.plugin.pdfutilities.features.AnnotationController;
import org.docear.plugin.pdfutilities.features.AnnotationID;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.features.AnnotationNodeModel;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.pdf.PdfFileFilter;
import org.docear.plugin.pdfutilities.ui.MonitoringDialog;
import org.docear.plugin.pdfutilities.ui.conflict.ImportConflictDialog;
import org.docear.plugin.pdfutilities.util.CustomFileFilter;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.docear.plugin.pdfutilities.util.Tools;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.mindmapmode.SaveAll;
import org.jdesktop.swingworker.SwingWorker;

import de.intarsys.pdf.cos.COSRuntimeException;
import de.intarsys.pdf.parser.COSLoadException;

@EnabledAction( checkOnNodeChange = true )
public abstract class AbstractMonitoringAction extends AFreeplaneAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AbstractMonitoringAction(String key) {
		super(key);
	}

	public abstract void setEnabled();

	public AbstractMonitoringAction(String key, String title, Icon icon) {
		super(key, title, icon);
	}

	protected void updateNodesAgainstMonitoringDir(final NodeModel target, final URI monitoringDir, final URI mindmapDir) {
		if(target == null || monitoringDir == null || mindmapDir == null) return;
		
		new SaveAll().actionPerformed(null);
					
		SwingWorker<Map<AnnotationID, Collection<IAnnotation>>, AnnotationModel[]> thread = new SwingWorker<Map<AnnotationID, Collection<IAnnotation>>, AnnotationModel[]>(){
			
					
			@Override
			protected Map<AnnotationID, Collection<IAnnotation>> doInBackground() throws Exception {
				firePropertyChange(MonitoringDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
				Map<AnnotationID, Collection<IAnnotation>> conflicts = new HashMap<AnnotationID, Collection<IAnnotation>>();
				URI monDir = Tools.getAbsoluteUri(monitoringDir);
				URI mapDir = Tools.getAbsoluteUri(mindmapDir);
				Collection<URI> monitorFiles = Tools.getFilteredFileList(monDir, new PdfFileFilter(), true);
				Collection<URI> mindmapFiles = Tools.getFilteredFileList(mapDir, new CustomFileFilter(".*[.][mM][mM]"), true);
				if(!mindmapFiles.contains(Controller.getCurrentController().getMap().getFile().toURI())){
					mindmapFiles.add(Controller.getCurrentController().getMap().getFile().toURI());
				}
				Map<AnnotationID, Collection<AnnotationNodeModel>> oldAnnotations = new NodeUtils().getOldAnnotationsFromMaps(mindmapFiles);
				int count = 0;
				firePropertyChange(MonitoringDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
				for(final URI uri : monitorFiles){
					try{
						if(Thread.currentThread().isInterrupted()) return conflicts;
						firePropertyChange(MonitoringDialog.NEW_FILE, null, Tools.getFilefromUri(uri).getName());
						PdfAnnotationImporter importer = new PdfAnnotationImporter();
						Collection<AnnotationModel> annotations = importer.importAnnotations(uri);
						AnnotationModel root = new AnnotationModel(new AnnotationID(Tools.getAbsoluteUri(uri), 0), AnnotationType.PDF_FILE);
						root.setTitle(Tools.getFilefromUri(Tools.getAbsoluteUri(uri)).getName());
						root.getChildren().addAll(annotations);
						annotations = new ArrayList<AnnotationModel>();
						annotations.add(root);
						annotations = AnnotationController.markNewAnnotations(annotations, oldAnnotations);
						AnnotationController.addConflictedAnnotations(AnnotationController.getConflictedAnnotations(annotations, oldAnnotations), conflicts);
						
						final Collection<AnnotationModel> finalAnnotations = annotations;
						SwingUtilities.invokeAndWait(
						        new Runnable() {
						            public void run(){
						            	new NodeUtils().insertNewChildNodesFrom(uri, finalAnnotations, target.isLeft(), target);
										//foldAll(target);
										firePropertyChange(MonitoringDialog.NEW_NODES, null, getInsertedNodes(finalAnnotations));										
						            }
						        }
						   );						
						count++;
						setProgress(100 * count / monitorFiles.size());
					} catch(IOException e){
						LogUtils.severe("IOexception during update file: "+ uri);
					} catch(COSRuntimeException e){
						LogUtils.severe("COSRuntimeException during update file: "+ uri);
					} catch(COSLoadException e){
						LogUtils.severe("COSLoadException during update file: "+ uri);
					}
				}					
				return conflicts;
			}		
			
			@Override
		    protected void done() {
				firePropertyChange(MonitoringDialog.IS_DONE, null, null);
			}
			
			private Collection<AnnotationModel> getInsertedNodes(Collection<AnnotationModel> annotations){
				Collection<AnnotationModel> result = new ArrayList<AnnotationModel>();
				for(AnnotationModel annotation : annotations){
					if(annotation.isNew()){
						result.add(annotation);
					}
					if(annotation.hasNewChildren()){
						result.addAll(this.getInsertedNodes(annotation.getChildren()));
					}
				}
				return result;
			}
			
		};
		
		try {
			MonitoringDialog monitoringDialog = new MonitoringDialog(Controller.getCurrentController().getViewController().getJFrame());
			monitoringDialog.showDialog(thread);
			
			Map<AnnotationID, Collection<IAnnotation>> conflicts = thread.get();
			if(conflicts != null && conflicts.size() > 0){
				ImportConflictDialog dialog = new ImportConflictDialog(Controller.getCurrentController().getViewController().getJFrame(), conflicts);
				dialog.showDialog();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CancellationException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
					
	}
	
	protected void foldAll(final NodeModel node) {
		final MapController modeController = Controller.getCurrentModeController().getMapController();
		for (NodeModel child : modeController.childrenUnfolded(node)) {
			foldAll(child);
		}
		setFolded(node, true);
	}
	
	protected void setFolded(final NodeModel node, final boolean state) {
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		if (mapController.hasChildren(node) && (mapController.isFolded(node) != state)) {
			mapController.setFolded(node, state);
		}
	}

}