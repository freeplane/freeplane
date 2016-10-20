package org.freeplane.features.presentations.mindmapmode;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JTabbedPane;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.components.JAutoScrollBarPane;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.presentations.mindmapmode.CollectionChangedEvent.EventType;

public class PresentationController implements IExtension{
	private final PresentationState presentationState;
	private final PresentationEditorController presentationEditorController;
	private ModeController modeController;
	
	public static void install(final ModeController modeController) {
		final PresentationController presentationController = new PresentationController(modeController);
		presentationController.registerActions();
		presentationController.addMapSelectionListener();
		new PresentationBuilder().register(modeController.getMapController(), presentationController);
		final JTabbedPane tabs = (JTabbedPane) modeController.getUserInputListenerFactory().getToolBar("/format").getComponent(1);
		tabs.add("Presentations", presentationController.createPanel());

	}

	private PresentationController(ModeController modeController) {
		this.modeController = modeController;
		presentationState = new PresentationState();
		presentationEditorController = new PresentationEditorController(presentationState);
	}

	private void registerActions() {
		modeController.addAction(new ShowCurrentSlideAction(presentationState));
		modeController.addAction(new ShowPreviousSlideAction(presentationState));
		modeController.addAction(new ShowNextSlideAction(presentationState));
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
					if(event.eventType == EventType.COLLECTION_SIZE_CHANGED)
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
				if(event.eventType == EventType.COLLECTION_SIZE_CHANGED)
					modeController.getMapController().setSaved(map, false);
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
		return new JAutoScrollBarPane(presentationEditorController.createPanel());
	}
}

@SuppressWarnings("serial")
@SelectableAction
class ShowCurrentSlideAction extends AFreeplaneAction {
	private PresentationState presentationState;

	public ShowCurrentSlideAction(PresentationState presentationState) {
		super("ShowCurrentSlideAction");
		this.presentationState = presentationState;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (presentationState.isPresentationRunning())
			presentationState.stopPresentation();
		else if (presentationState.canShowCurrentSlide())
			presentationState.showSlide();
		setSelected(presentationState.isPresentationRunning());
	}

	@Override
	public void afterMapChange(final Object newMap) {
	}
}

@SuppressWarnings("serial")
class ShowNextSlideAction extends AFreeplaneAction {
	private final PresentationState presentationState;

	public ShowNextSlideAction(PresentationState presentationState) {
		super("ShowNextSlideAction");
		this.presentationState = presentationState;
		presentationState.addPresentationStateListener(new PresentationStateChangeListener() {
			@Override
			public void onPresentationStateChange(PresentationStateChangeEvent presentationStateChangeEvent) {
				setEnabled(ShowNextSlideAction.this.presentationState.canShowNextSlide());
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (presentationState.canShowNextSlide())
			presentationState.showNextSlide();
	}

	@Override
	public void afterMapChange(final Object newMap) {
	}

}

@SuppressWarnings("serial")
class ShowPreviousSlideAction extends AFreeplaneAction {
	private PresentationState presentationState;

	public ShowPreviousSlideAction(PresentationState presentationState) {
		super("ShowPreviousSlideAction");
		this.presentationState = presentationState;
		presentationState.addPresentationStateListener(new PresentationStateChangeListener() {
			@Override
			public void onPresentationStateChange(PresentationStateChangeEvent presentationStateChangeEvent) {
				setEnabled(ShowPreviousSlideAction.this.presentationState.canShowPreviousSlide());
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (presentationState.canShowPreviousSlide())
			presentationState.showPreviousSlide();
	}

	@Override
	public void afterMapChange(final Object newMap) {
	}
}
