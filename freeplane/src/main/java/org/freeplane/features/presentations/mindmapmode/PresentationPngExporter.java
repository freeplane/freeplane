package org.freeplane.features.presentations.mindmapmode;

import static org.freeplane.features.presentations.mindmapmode.PresentationAutomation.SWITCH_TO_FULL_SCREEN_PROPERTY;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.export.mindmapmode.ExportToImage;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.view.swing.map.MapView;

class PresentationPngExporter {
	
	static void exportPresentation(PresentationState presentationState) {
		final PresentationPngExporterFactory presentationPngExporterFactory = new PresentationPngExporterFactory(presentationState);
		final PresentationPngExporter exporter = presentationPngExporterFactory.exporter;
		if(exporter != null) {
			exporter.exportSinglePresentation();
		}
	}
	
	static void exportPresentations(PresentationState presentationState) {
		final PresentationPngExporterFactory presentationPngExporterFactory = new PresentationPngExporterFactory(presentationState);
		final PresentationPngExporter exporter = presentationPngExporterFactory.exporter;
		if(exporter != null) {
			exporter.exportAllPresentations();
		}
		
	}
	
	static class ActionInstaller {
		public void installActions(ModeController modeController, PresentationState state){
			modeController.addAction(new ExportAllPresentationsAction(state));
			modeController.addAction(new ExportPresentationAction(state));
		}
	}
	
	@SuppressWarnings("serial")
	private static class ExportAllPresentationsAction extends AFreeplaneAction{

		private final PresentationState state;

		public ExportAllPresentationsAction(PresentationState state) {
			super("ExportAllPresentationsAction");
			this.state = state;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			PresentationPngExporter.exportPresentations(state);
		}
		
	}

	@SuppressWarnings("serial")
	private static class ExportPresentationAction extends AFreeplaneAction{

		private final PresentationState state;

		public ExportPresentationAction(PresentationState state) {
			super("ExportPresentationAction");
			this.state = state;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			PresentationPngExporter.exportPresentation(state);
		}
		
	}
	
	private static class PresentationPngExporterFactory{

		private NamedElementCollection<Presentation> presentations;
		private PresentationPngExporter exporter;

		public PresentationPngExporterFactory(PresentationState presentationState) {
			final PresentationController presentationController = Controller.getCurrentModeController().getExtension(PresentationController.class);
			MapModel map = Controller.getCurrentController().getMap();
			if(map == null)
				return;
			presentations = presentationController.getPresentations(map).presentations;
			if(presentations.getSize() == 0)
				return;
			File file = map.getFile();
			final String name = ResourceController.getResourceController().getProperty("presentation.exportDirectory");
			final String validName = FileUtils.validFileNameOf(name);
			final File presentationExportDirectory = new File (validName.isEmpty() ? "exportDirectory" : validName);

			if (file == null  && ! presentationExportDirectory.isAbsolute()) {
				JOptionPane.showMessageDialog(Controller.getCurrentController().getViewController().getCurrentRootComponent(),
						TextUtils.getText("map_not_saved"), "Freeplane", JOptionPane.WARNING_MESSAGE);
				return;
			}
			final File exportDirectory =
					presentationExportDirectory.isAbsolute() ? presentationExportDirectory :
						new File(file.getAbsoluteFile().getParentFile(), presentationExportDirectory.getPath());

			exporter = new PresentationPngExporter(presentationState, exportDirectory);
		}
		
	}
	
	private final File exportDirectory;
	private final PresentationState presentationState;
	private final float zoom;
	private final NodeModel[] selection;
	private boolean presentationSlowMotionEnabled;
	private boolean spotlightEnabledForExport;
	private final JComponent mapViewComponent;
	private float presentationZoomFactor;

	private PresentationPngExporter(PresentationState presentationState, File exportDirectory) {
		this.presentationState = presentationState;
		this.exportDirectory = exportDirectory;
		this.zoom = Controller.getCurrentController().getMapViewManager().getZoom();
		final List<NodeModel> selection = Controller.getCurrentController().getSelection().getOrderedSelection();
		this.selection = selection.toArray(new NodeModel[selection.size()]);
		mapViewComponent = (JComponent) Controller.getCurrentController().getMapViewManager().getMapViewComponent();
	}
	
	private void exportAllPresentations() {
		prepareExport();
		NamedElementCollection<Presentation> presentations = presentationState.getPresentations();
		for(int i = 0; i < presentations.getSize(); i++)
	    	exportPresentation(presentations.getElement(i));
		restorePreviousPresentation();
		UrlManager.getController().loadURL(exportDirectory.toURI());
	}

	private void prepareExport() {
		presentationSlowMotionEnabled = ResourceController.getResourceController().getBooleanProperty(Slide.PRESENTATION_SLOW_MOTION_KEY, false);
		if (presentationState.isPresentationRunning())
			presentationZoomFactor = presentationState.getPresentationZoomFactor();
		else if(presentationState.usesMapZoom())
			presentationZoomFactor = Controller.getCurrentController().getMapViewManager().getZoom();
		else
			presentationZoomFactor = 1f;
		ResourceController.getResourceController().setProperty(Slide.PRESENTATION_SLOW_MOTION_KEY, false);
		if(ResourceController.getResourceController().getBooleanProperty(PresentationAutomation.SWITCH_TO_SPOTLIGHT_PROPERTY)) {
			if (! Boolean.TRUE.equals(mapViewComponent.getClientProperty(MapView.SPOTLIGHT_ENABLED))) {
					mapViewComponent.putClientProperty(MapView.SPOTLIGHT_ENABLED, true);
					spotlightEnabledForExport = true;
			}
		}
	}
	

	private void exportSinglePresentation() {
		prepareExport();
		NamedElementCollection<Presentation> presentations = presentationState.getPresentations();
		exportPresentation(presentations.getCurrentElement());
		restorePreviousPresentation();
		UrlManager.getController().loadURL(exportDirectory.toURI());
	}

	private void restorePreviousPresentation() {
		presentationState.restore();
		presentationZoomFactor = 1f;
		final IMapSelection selectionController = Controller.getCurrentController().getSelection();
		selectionController.replaceSelection(selection);
		if(! presentationState.isPresentationRunning())
			Controller.getCurrentController().getMapViewManager().setZoom(zoom);
		ResourceController.getResourceController().setProperty(Slide.PRESENTATION_SLOW_MOTION_KEY, presentationSlowMotionEnabled);
		if(spotlightEnabledForExport)
			mapViewComponent.putClientProperty(MapView.SPOTLIGHT_ENABLED, null);
		selectionController.scrollNodeToVisible(selectionController.getSelected());
	}

	public void exportPresentation(Presentation p) {
		final String validName = FileUtils.validFileNameOf(p.getName());
		if(validName.isEmpty())
			return;
		exportDirectory.mkdir();
		if(! exportDirectory.isDirectory())
			return;
		File presentationDirectory = new File(exportDirectory, validName);
		presentationDirectory.mkdir();
		if(! exportDirectory.isDirectory())
			return;
		NamedElementCollection<Slide> slides = p.slides;
        for(int i = 0; i < slides.getSize(); i++)
        	exportSlide(presentationDirectory, slides.getElement(i));
	}

	private void exportSlide(File presentationDirectory, Slide slide) {
		final NodeModel placedNode = slide.getCurrentPlacedNode();
		if(placedNode != null)
			slide.apply(presentationZoomFactor);
		else
			slide.apply(1f);
		mapViewComponent.validate();
		mapViewComponent.setSize(mapViewComponent.getPreferredSize());
		File exportFile = new File(presentationDirectory, FileUtils.validFileNameOf(slide.getName()) + ".png");
		final ExportToImage exporter = ExportToImage.toPNG();
		final Controller controller = Controller.getCurrentController();
		final MapModel map = controller.getMap();
		if(placedNode != null) {
			final Dimension slideSize;
			if(ResourceController.getResourceController().getBooleanProperty(SWITCH_TO_FULL_SCREEN_PROPERTY))
				slideSize = mapViewComponent.getGraphicsConfiguration().getBounds().getSize();
			else
				slideSize = SwingUtilities.getWindowAncestor(mapViewComponent).getSize();
			exporter.export(map, slideSize, placedNode, slide.getPlacedNodePosition(), exportFile);
		} else
			exporter.export(map, exportFile);
	}
}
