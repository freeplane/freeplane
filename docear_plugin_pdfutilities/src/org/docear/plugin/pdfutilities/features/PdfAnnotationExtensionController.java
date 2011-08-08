package org.docear.plugin.pdfutilities.features;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.map.MapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class PdfAnnotationExtensionController implements IExtension{
	
	public static PdfAnnotationExtensionController getController() {
		return getController(Controller.getCurrentModeController());
	}

	public static PdfAnnotationExtensionController getController(ModeController modeController) {
		return (PdfAnnotationExtensionController) modeController.getExtension(PdfAnnotationExtensionController.class);
	}
	public static void install( final PdfAnnotationExtensionController annotationController) {
		Controller.getCurrentModeController().addExtension(PdfAnnotationExtensionController.class, annotationController);
	}
	
	public PdfAnnotationExtensionController(final ModeController modeController){
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		final ReadManager readManager = mapController.getReadManager();
		final WriteManager writeManager = mapController.getWriteManager();
		PdfAnnotationExtensionBuilder builder = new PdfAnnotationExtensionBuilder();
		builder.registerBy(readManager, writeManager);
	}

}
