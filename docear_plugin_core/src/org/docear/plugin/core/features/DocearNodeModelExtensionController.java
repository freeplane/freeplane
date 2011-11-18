package org.docear.plugin.core.features;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class DocearNodeModelExtensionController implements IExtension{
	
	public static DocearNodeModelExtensionController getController() {
		return getController(Controller.getCurrentModeController());
	}

	public static DocearNodeModelExtensionController getController(ModeController modeController) {
		return (DocearNodeModelExtensionController) modeController.getExtension(DocearNodeModelExtensionController.class);
	}
	public static void install( final DocearNodeModelExtensionController docearNodeModelController) {
		Controller.getCurrentModeController().addExtension(DocearNodeModelExtensionController.class, docearNodeModelController);
	}
	
	public DocearNodeModelExtensionController(final ModeController modeController){
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		DocearNodeModelExtensionXmlBuilder builder = new DocearNodeModelExtensionXmlBuilder();
		builder.registerBy(readManager, writeManager);
	}
	
	public static DocearNodeModelExtension getModel(final NodeModel node) {
		DocearNodeModelExtension docearNodeModel = (DocearNodeModelExtension) node.getExtension(DocearNodeModelExtension.class);		
		return docearNodeModel;
	}
	
	public static void setModel(final NodeModel node, final DocearNodeModelExtension docearNodeModel) {
		final DocearNodeModelExtension olddocearNodeModel = (DocearNodeModelExtension) node.getExtension(DocearNodeModelExtension.class);
		if (docearNodeModel != null && olddocearNodeModel == null) {
			node.addExtension(docearNodeModel);
		}
		else if (docearNodeModel == null && olddocearNodeModel != null) {
			node.removeExtension(DocearNodeModelExtension.class);
		}
	}
	public static DocearNodeModelExtension setEntry(NodeModel node, String key, Object value){
		DocearNodeModelExtension extension = getModel(node);
		if(extension == null){
			extension = new DocearNodeModelExtension();
			extension.putEntry(key, value);
			setModel(node, extension);
		}
		else{
			extension.putEntry(key, value);
		}
		return extension;
	}

}
