package org.freeplane.features.presentations.mindmapmode;

import org.freeplane.core.undo.IActor;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class UndoableNamedElementCollection<T extends NamedElement<T>> {
	final private ModeController controller;
	final private MapModel mapModel;
	final private NamedElementCollection<T> collection;
	
	public static <T extends NamedElement<T>> UndoableNamedElementCollection<T> of(NamedElementCollection<T> collection){
		return new UndoableNamedElementCollection<>(Controller.getCurrentModeController(), Controller.getCurrentController().getMap(), collection);
	}
	
	public UndoableNamedElementCollection(ModeController controller, MapModel mapModel,
			NamedElementCollection<T> collection) {
		super();
		this.controller = controller;
		this.mapModel = mapModel;
		this.collection = collection;
	}
	public void add(final String name) {
		final int currentElementIndex = collection.getCurrentElementIndex();
		IActor actor = new IActor() {
			@Override
			public String getDescription() {
				return "add";
			}

			@Override
			public void act() {
				collection.selectCurrentElement(currentElementIndex);
				collection.add(name);
			}
			
			@Override
			public void undo() {
				collection.selectCurrentElement(currentElementIndex + 1);
				collection.removeCurrentElement();
				collection.selectCurrentElement(currentElementIndex);
			}
		};
		controller.execute(actor, mapModel);
	}
	
	public void add(final T element) {
		final int currentElementIndex = collection.getCurrentElementIndex();
		IActor actor = new IActor() {
			@Override
			public String getDescription() {
				return "add";
			}

			@Override
			public void act() {
				collection.selectCurrentElement(currentElementIndex);
				collection.add(element);
			}
			
			@Override
			public void undo() {
				collection.selectCurrentElement(currentElementIndex + 1);
				collection.removeCurrentElement();
				collection.selectCurrentElement(currentElementIndex);
			}
		};
		controller.execute(actor, mapModel);
	}
	
	public void removeCurrentElement() {
		final int currentElementIndex = collection.getCurrentElementIndex();
		final T removedElement = collection.getCurrentElement();
		IActor actor = new IActor() {
			@Override
			public String getDescription() {
				return "removeCurrentElement";
			}

			@Override
			public void act() {
				collection.selectCurrentElement(currentElementIndex);
				collection.removeCurrentElement();
			}
			
			@Override
			public void undo() {
				collection.selectCurrentElement(currentElementIndex - 1);
				collection.add(removedElement);
			}
		};
		controller.execute(actor, mapModel);
	}
	
	public void moveCurrentElementUp() {
		moveCurrentElementTo(collection.getCurrentElementIndex() + 1);
	}
	
	public void moveCurrentElementDown() {
		moveCurrentElementTo(collection.getCurrentElementIndex() - 1);
	}

	public void moveCurrentElementTo(final int newElementIndex) {
		if(! collection.canMoveCurrentElementTo(newElementIndex))
			return;
		final int oldElementIndex = collection.getCurrentElementIndex();
		IActor actor = new IActor() {
			@Override
			public String getDescription() {
				return "moveCurrentElementTo";
			}

			@Override
			public void act() {
				collection.selectCurrentElement(oldElementIndex);
				collection.moveCurrentElementTo(newElementIndex);
			}
			
			@Override
			public void undo() {
				collection.selectCurrentElement(newElementIndex);
				collection.moveCurrentElementTo(oldElementIndex);
			}
		};
		controller.execute(actor, mapModel);
	}
	
	
}
