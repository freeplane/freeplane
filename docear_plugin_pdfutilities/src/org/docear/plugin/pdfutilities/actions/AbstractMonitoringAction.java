package org.docear.plugin.pdfutilities.actions;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.Icon;

import org.docear.plugin.pdfutilities.features.AnnotationController;
import org.docear.plugin.pdfutilities.features.AnnotationID;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.features.AnnotationNodeModel;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.pdf.PdfFileFilter;
import org.docear.plugin.pdfutilities.ui.MonitoringDialog;
import org.docear.plugin.pdfutilities.ui.conflict.ImportConflictDialog;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.docear.plugin.pdfutilities.util.Tools;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
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

	protected void updateNodesAgainstMonitoringDir(final NodeModel target, URI monitoringDir, URI mindmapDir) {
					
					monitoringDir = Tools.getAbsoluteUri(monitoringDir);
					final Collection<URI> monitorFiles = Tools.getFilteredFileList(monitoringDir, new PdfFileFilter(), true);
					final Map<AnnotationID, Collection<AnnotationNodeModel>> oldAnnotations = new NodeUtils().getOldAnnotationsFromCurrentMap();
					
					SwingWorker<Map<AnnotationID, Collection<IAnnotation>>, AnnotationModel[]> thread = new SwingWorker<Map<AnnotationID, Collection<IAnnotation>>, AnnotationModel[]>(){
			
						@Override
						protected Map<AnnotationID, Collection<IAnnotation>> doInBackground() throws Exception {
							Map<AnnotationID, Collection<IAnnotation>> conflicts = new HashMap<AnnotationID, Collection<IAnnotation>>();
							int count = 0;
							for(URI uri : monitorFiles){
								try{
									if(Thread.currentThread().isInterrupted()) return conflicts;
									firePropertyChange("newFile", null, Tools.getFilefromUri(uri).getName());
									PdfAnnotationImporter importer = new PdfAnnotationImporter();
									Collection<AnnotationModel> annotations = importer.importAnnotations(uri);
									annotations = AnnotationController.markNewAnnotations(annotations, oldAnnotations);
									AnnotationController.addConflictedAnnotations(AnnotationController.getConflictedAnnotations(annotations, oldAnnotations), conflicts);
									
									publish(annotations.toArray(new AnnotationModel[annotations.size()]));
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
					    protected void process(List<AnnotationModel[]> chunks) {
							for(AnnotationModel[] chunk : chunks){
								new NodeUtils().insertNewChildNodesFrom(Arrays.asList(chunk), target.isLeft(), target);
								firePropertyChange("newNodes", null, getInsertedNodes(Arrays.asList(chunk)));
							}					
					     }
						
						@Override
					    protected void done() {
							firePropertyChange("isDone", null, null);
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
					}
					
			}

}