package org.freeplane.plugin.workspace.features;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IExtensionAttributeWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

public class WorkspaceModelExtensionWriterReader implements IExtensionAttributeWriter {

	private static final String PROJECT_ID_XML_TAG = "project";
	private static final String MAP_EXTENSION_XML_TAG = "map";
	private static final String PROJECT_HOME_XML_TAG = "project_last_home";
		
	private WorkspaceModelExtensionWriterReader(MapController mapController) {
		registerAttributeHandlers(mapController.getReadManager());
		mapController.getWriteManager().addExtensionAttributeWriter(WorkspaceMapModelExtension.class, this);	
		mapController.addMapLifeCycleListener(new IMapLifeCycleListener() {
			
			public void onRemove(MapModel map) {}
			
			public void onCreate(MapModel map) {
				WorkspaceMapModelExtension wmme = WorkspaceController.getMapModelExtension(map);
				if(wmme.getProject() == null) {
					//WORKSPACE - fixme: make sure a project is loaded and selected?
					try {
						WorkspaceController.addMapToProject(map, WorkspaceController.getCurrentProject());
					}
					catch (Exception e) {
						LogUtils.warn("Exception in "+this.getClass()+".onCreate(MapModel): no current project was selected");
					}
				}
			}
		});		
	}

	private void registerAttributeHandlers(ReadManager reader) {
		reader.addAttributeHandler(MAP_EXTENSION_XML_TAG, PROJECT_ID_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object map, String value) {
				final MapModel mapModel = (MapModel) map;
				
				WorkspaceMapModelExtension wmme = WorkspaceController.getMapModelExtension(mapModel); 
				if(wmme.getProject() == null) {
					AWorkspaceProject prj = WorkspaceController.getCurrentModel().getProject(value);
					if(prj == null) {
						//WORKSPACE - todo: propagate exception
						LogUtils.warn("project with id="+value+" was not found");
						return;
					}
					wmme.setProject(prj);
				}
			}			
		});
		
		reader.addAttributeHandler(MAP_EXTENSION_XML_TAG, PROJECT_HOME_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object map, String value) {
				final MapModel mapModel = (MapModel) map;
				
				WorkspaceMapModelExtension wmme = WorkspaceController.getMapModelExtension(mapModel); 
				if(wmme.getProject() == null) {
					//WORKSPACE - todo: find project for uri?
//					AWorkspaceProject prj = WorkspaceController.getCurrentModel().getProject(value);
//					if(prj == null) {
//						//WORKSPACE - todo: propagate exception
//						LogUtils.warn("project with id="+value+" was not found");
//						return;
//					}
//					wmme.setProject(prj);
				}
				else {
					//ignore
				}
			}			
		});
	}

	public void writeAttributes(ITreeWriter writer, Object userObject, IExtension extension) {
		final WorkspaceMapModelExtension wmme = extension != null ? (WorkspaceMapModelExtension) extension : WorkspaceController.getMapModelExtension(((NodeModel) userObject).getMap());
		AWorkspaceProject prj = wmme.getProject();
		
		if(prj == null) {
			return;
		}		
		writer.addAttribute(PROJECT_ID_XML_TAG, prj.getProjectID());
		writer.addAttribute(PROJECT_HOME_XML_TAG, prj.getProjectHome().toString());
	}

	public static void register(ModeController modeController) {
		new WorkspaceModelExtensionWriterReader(modeController.getMapController());
	}
	
	
}
