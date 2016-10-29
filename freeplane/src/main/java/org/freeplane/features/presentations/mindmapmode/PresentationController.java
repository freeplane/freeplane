package org.freeplane.features.presentations.mindmapmode;

import static org.freeplane.features.presentations.mindmapmode.CollectionChangedEvent.EventType.COLLECTION_SIZE_CHANGED;
import static org.freeplane.features.presentations.mindmapmode.CollectionChangedEvent.EventType.SELECTION_CHANGED;
import static org.freeplane.features.presentations.mindmapmode.PresentationStateChangeEvent.EventType.HIGNLIGHTING_CHANGED;
import static org.freeplane.features.presentations.mindmapmode.PresentationStateChangeEvent.EventType.PLAYING_STATE_CHANGED;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.JTabbedPane;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.components.JAutoScrollBarPane;
import org.freeplane.features.highlight.HighlightController;
import org.freeplane.features.highlight.NodeHighlighter;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class PresentationController implements IExtension{
	private static final Color NODE_HIGHLIGHTING_COLOR = Color.GREEN.brighter();
	private final PresentationState presentationState;
	private final PresentationEditorController presentationEditorController;
	ModeController modeController;
	
	public static void install(final ModeController modeController) {
		final PresentationController presentationController = new PresentationController(modeController);
		presentationController.registerActions();
		presentationController.addMapSelectionListener();
		new PresentationBuilder().register(modeController.getMapController(), presentationController);
		final JTabbedPane tabs = (JTabbedPane) modeController.getUserInputListenerFactory().getToolBar("/format").getComponent(1);
		tabs.add("Presentations", presentationController.createPanel());
		modeController.getController().getExtension(HighlightController.class).addNodeHighlighter(new NodeHighlighter() {
			
			@Override
			public boolean isNodeHighlighted(NodeModel node) {
				return presentationController.isNodeHighlighted(node);
			}
			
			@Override
			public Color getColor() {
				return NODE_HIGHLIGHTING_COLOR;
			}
		});

	}

	private void registerActions() {
		presentationEditorController.registerActions(modeController);
	}

	boolean isNodeHighlighted(NodeModel node) {
		return presentationState.isNodeHighlighted(node) && ! presentationState.isPresentationRunning();
	}

	private PresentationController(ModeController modeController) {
		this.modeController = modeController;
		presentationState = new PresentationState();
		presentationEditorController = new PresentationEditorController(presentationState);
		presentationState.addPresentationStateListener(new PresentationStateChangeListener() {
			
			@Override
			public void onPresentationStateChange(PresentationStateChangeEvent presentationStateChangeEvent) {
				repaintMap();
			}
		});
	}

	private void addMapSelectionListener() {
		IMapSelectionListener mapSelectionListener = new IMapSelectionListener() {
			
			@Override
			public void beforeMapChange(MapModel oldMap, MapModel newMap) {
			}
			
			@Override
			public void afterMapChange(MapModel oldMap, MapModel newMap) {
				if(newMap != null && Controller.getCurrentModeController() == modeController)
					presentationEditorController.setPresentations(getPresentations(newMap).presentations);
				else
					presentationEditorController.setPresentations(null);
			}

		};
		modeController.getController().getMapViewManager().addMapSelectionListener(mapSelectionListener);
	}
	public MapPresentations getPresentations(final MapModel map) {
		final NodeModel rootNode = map.getRootNode();
		MapPresentations mapPresentations = rootNode.getExtension(MapPresentations.class);
		if(mapPresentations == null) {
			mapPresentations = new MapPresentations(getPresentationFactory(map));
			final CollectionChangeListener<Presentation> presentationCollectionChangeListener = new CollectionChangeListener<Presentation>() {
				
				@Override
				public void onCollectionChange(CollectionChangedEvent<Presentation> event) {
					if(event.eventType == COLLECTION_SIZE_CHANGED)
						modeController.getMapController().setSaved(map, false);
				}
			};
			mapPresentations.presentations.addCollectionChangeListener(presentationCollectionChangeListener);
			rootNode.addExtension(mapPresentations);
		}
		return mapPresentations;
	}
	
	NamedElementFactory<Presentation> getPresentationFactory(final MapModel map) {
		final NamedElementFactory<Slide> slideFactory = getSlideFactory(map);
		
		final CollectionChangeListener<Slide> slideCollectionChangeListener = new CollectionChangeListener<Slide>() {
			
			@Override
			public void onCollectionChange(CollectionChangedEvent<Slide> event) {
				if(event.eventType == COLLECTION_SIZE_CHANGED)
					modeController.getMapController().setSaved(map, false);
				else if(event.eventType == SELECTION_CHANGED)
					presentationState.changeSlide();
			}
		};
		final NamedElementFactory<Presentation> presentationFactory = new NamedElementFactory<Presentation>() {
			
			@Override
			public Presentation create(Presentation prototype, String newName) {
				final Presentation presentation = prototype.saveAs(newName);
				presentation.slides.addCollectionChangeListener(slideCollectionChangeListener);
				return presentation;
			}
			
			@Override
			public Presentation create(String name) {
				final Presentation presentation = new Presentation(name, slideFactory);
				presentation.slides.addCollectionChangeListener(slideCollectionChangeListener);
				return presentation;
			}
		};
		return presentationFactory;
	}

	NamedElementFactory<Slide> getSlideFactory(final MapModel map) {
		final NamedElementFactory<Slide> slideFactory = new NamedElementFactory<Slide>() {
			final SlideChangeListener slideChangeListener = new SlideChangeListener() {
				@Override
				public void onSlideModelChange(SlideChangeEvent changeEvent) {
					modeController.getMapController().setSaved(map, false);
					presentationState.changeSlide();
				}
			};
			
			@Override
			public Slide create(Slide prototype, String newName) {
				final Slide slide = prototype.saveAs(newName);
				slide.addSlideChangeListener(slideChangeListener);
				return slide;
			}
			
			@Override
			public Slide create(String name) {
				final Slide slide = new Slide(name);
				slide.addSlideChangeListener(slideChangeListener);
				return slide;
			}
		};
		return slideFactory;
	}

	private Component createPanel() {
		final Component presentationEditor = presentationEditorController.createPanel();
		presentationEditor.addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				if( 0 != (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED))
					presentationState.setHighlightsNodes(e.getComponent().isShowing());
				
			}
		});
		return new JAutoScrollBarPane(presentationEditor);
	}

	private void repaintMap() {
		final Component mapViewComponent = Controller.getCurrentController().getMapViewManager().getMapViewComponent();
		if(mapViewComponent != null)
			mapViewComponent.repaint();
	}
}
