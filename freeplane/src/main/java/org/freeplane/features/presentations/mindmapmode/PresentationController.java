package org.freeplane.features.presentations.mindmapmode;

import static org.freeplane.features.presentations.mindmapmode.CollectionChangedEvent.EventType.COLLECTION_SIZE_CHANGED;
import static org.freeplane.features.presentations.mindmapmode.CollectionChangedEvent.EventType.SELECTION_CHANGED;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.JTabbedPane;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.JAutoScrollBarPane;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.highlight.HighlightController;
import org.freeplane.features.highlight.NodeHighlighter;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class PresentationController implements IExtension{
	private static final float FOLDED_NODE_DOT_WIDTH = 3f * UITools.FONT_SCALE_FACTOR;
	private static final Color NODE_HIGHLIGHTING_COLOR = Color.GREEN.brighter();
	static final String PROCESS_NAVIGATION_KEYS_PROPERTY = "presentation.processesNavigationKeys";
	static final String PROCESS_ESCAPE_KEY_PROPERTY = "presentation.processesEscapeKey";
	

	private static float[] FOLDED_NODE_DASH = new float[]{FOLDED_NODE_DOT_WIDTH/2, 2*FOLDED_NODE_DOT_WIDTH};
	private static BasicStroke FOLDED_NODE_STROKE = new BasicStroke(FOLDED_NODE_DOT_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1f, FOLDED_NODE_DASH, 0f);
	private final PresentationState presentationState;
	private final PresentationEditorController presentationEditorController;
	ModeController modeController;

	public static void install(final ModeController modeController) {
		final PresentationController presentationController = new PresentationController(modeController);
		modeController.addExtension(PresentationController.class, presentationController);
		presentationController.registerActions();
		presentationController.addMapSelectionListener();
		new PresentationBuilder().register(modeController.getMapController(), presentationController);
		HighlightController highlightController = modeController.getController().getExtension(HighlightController.class);
		final PresentationState presentationState = presentationController.presentationState;
		new PresentationPngExporter.ActionInstaller().installActions(modeController, presentationState);
		final JTabbedPane tabs = (JTabbedPane) modeController.getUserInputListenerFactory().getToolBar("/format").getComponent(1);
		tabs.add("Presentations", presentationController.createPanel());
		highlightController.addNodeHighlighter(new NodeHighlighter() {
			
			@Override
			public boolean isNodeHighlighted(NodeModel node, boolean isPrinting) {
				return !isPrinting && presentationState.shouldHighlightNodeContainedOnSlide(node);
			}
			
			@Override
			public void configure(Graphics2D g, boolean isPrinting) {
				g.setColor(NODE_HIGHLIGHTING_COLOR);
			}

		});
		highlightController.addNodeHighlighter(new NodeHighlighter() {
			
			@Override
			public boolean isNodeHighlighted(NodeModel node, boolean isPrinting) {
				return !isPrinting && presentationState.shouldHighlightNodeFoldedOnSlide(node);
			}
			
			@Override
			public void configure(Graphics2D g, boolean isPrinting) {
				g.setColor(NODE_HIGHLIGHTING_COLOR);
				g.setStroke(FOLDED_NODE_STROKE);
			}

		});
		
		KeyEventDispatcher navigationKeyEventDispatcher = new NavigationKeyEventDispatcher(presentationState);
		KeyEventDispatcher escapeKeyEventDispatcher = new EscapeKeyEventDispatcher(presentationState);
		final PresentationAutomation presentationKeyHandler = new PresentationAutomation(presentationState, 
				PresentationKeyEventDispatcher.of(navigationKeyEventDispatcher, PROCESS_NAVIGATION_KEYS_PROPERTY),
				PresentationKeyEventDispatcher.of(escapeKeyEventDispatcher, PROCESS_ESCAPE_KEY_PROPERTY));
		presentationState.addPresentationStateListener(presentationKeyHandler);
	}

	private void registerActions() {
		presentationEditorController.registerActions(modeController);
	}

	private PresentationController(ModeController modeController) {
		this.modeController = modeController;
		presentationState = new PresentationState();
		final ResourceController resourceController = ResourceController.getResourceController();
		boolean combinesAllPresentations = resourceController.getBooleanProperty("presentation.combineAll");
		resourceController.addPropertyChangeListener(new IFreeplanePropertyListener() {
			
			@Override
			public void propertyChanged(String propertyName, String newValue, String oldValue) {
				if("presentation.combineAll".equals(propertyName))
					presentationState.setCombinesAllPresentations(Boolean.parseBoolean(newValue));
			}
		});
		presentationState.setCombinesAllPresentations(combinesAllPresentations);
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
				presentationState.stopPresentation();
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
		final Component presentationEditor = presentationEditorController.createPanel(modeController);
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
