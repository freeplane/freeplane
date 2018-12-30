/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.attribute.mindmapmode;

import java.util.NoSuchElementException;
import java.util.Vector;

import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.Quantity;
import org.freeplane.core.util.collection.SortedComboBoxModel;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.attribute.AttributeRegistryElement;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.attribute.mindmapmode.clipboard.MAttributeClipboardController;
import org.freeplane.features.clipboard.ClipboardControllers;
import org.freeplane.features.clipboard.mindmapmode.MClipboardControllers;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class MAttributeController extends AttributeController {

	static public MAttributeController getController(){
		return (MAttributeController) AttributeController.getController();
	}
	private class AttributeChanger implements IVisitor {
		final private Object name;
		final private Object newValue;
		final private Object oldValue;

		public AttributeChanger(final Object name, final Object oldValue, final Object newValue) {
			super();
			this.name = name;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * freeplane.modes.attributes.AttributeRegistry.Visitor#visit(freeplane
		 * .modes.attributes.ConcreteAttributeTableModel)
		 */
		@Override
		public void visit(final NodeModel node, final NodeAttributeTableModel model) {
			for (int i = 0; i < model.getRowCount(); i++) {
				if (model.getName(i).equals(name) && model.getValue(i).equals(oldValue)) {
					final int row = i;
					final IActor actor = new SetAttributeValueActor(node, model, row, newValue);
					Controller.getCurrentModeController().execute(actor, node.getMap());
				}
			}
		}
	}

	private class AttributeRemover implements IVisitor {
		final private Object name;

		public AttributeRemover(final Object name) {
			super();
			this.name = name;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * freeplane.modes.attributes.AttributeRegistry.Visitor#visit(freeplane
		 * .modes.attributes.ConcreteAttributeTableModel)
		 */
		@Override
		public void visit(final NodeModel node, final NodeAttributeTableModel model) {
			for (int i = 0; i < model.getRowCount(); i++) {
				if (model.getName(i).equals(name)) {
					final int row = i;
					final IActor actor = new RemoveAttributeActor(node, model, row);
					Controller.getCurrentModeController().execute(actor, node.getMap());
				}
			}
		}
	}

	private class AttributeRenamer implements IVisitor {
		final private Object newName;
		final private Object oldName;

		public AttributeRenamer(final Object oldName, final Object newName) {
			super();
			this.newName = newName;
			this.oldName = oldName;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * freeplane.modes.attributes.AttributeRegistry.Visitor#visit(freeplane
		 * .modes.attributes.ConcreteAttributeTableModel)
		 */
		@Override
		public void visit(final NodeModel node, final NodeAttributeTableModel model) {
			for (int i = 0; i < model.getRowCount(); i++) {
				if (model.getName(i).equals(oldName)) {
					final int row = i;
					final String name = newName.toString();
					final String oldName = this.oldName.toString();
					final IActor actor = new SetAttributeNameActor(node, model, name, oldName, row);
					Controller.getCurrentModeController().execute(actor, node.getMap());
				}
			}
		}
	}

	private class AttributeValueRemover implements IVisitor {
		final private Object name;
		final private Object value;

		public AttributeValueRemover(final Object name, final Object value) {
			super();
			this.name = name;
			this.value = value;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * freeplane.modes.attributes.AttributeRegistry.Visitor#visit(freeplane
		 * .modes.attributes.ConcreteAttributeTableModel)
		 */
		@Override
		public void visit(final NodeModel node, final NodeAttributeTableModel model) {
			for (int i = 0; i < model.getRowCount(); i++) {
				if (model.getName(i).equals(name) && model.getValue(i).equals(value)) {
					final IActor actor = new RemoveAttributeActor(node, model, i);
					Controller.getCurrentModeController().execute(actor, node.getMap());
				}
			}
		}
	}

	private class InsertAttributeActor implements IActor {
		private final NodeAttributeTableModel model;
		private final String name;
		private final int row;
		private final Object value;
		private final NodeModel node;

		private InsertAttributeActor(NodeModel node, final NodeAttributeTableModel model, final int row, final String name,
		                             final Object value) {
			this.node = node;
			this.row = row;
			this.name = name;
			this.model = model;
			this.value = value;
		}

		@Override
		public void act() {
			final Attribute newAttribute = new Attribute(name, value);
			model.getAttributes().add(row, newAttribute);
			model.fireTableRowsInserted(node, row, row);
		}

		@Override
		public String getDescription() {
			return "InsertAttributeActor";
		}

		@Override
		public void undo() {
			model.getAttributes().remove(row);
			model.fireTableRowsDeleted(node, row, row);
		}
	}

	private class Iterator {
		final private IVisitor visitor;

		Iterator(final IVisitor v) {
			visitor = v;
		}

		/**
		 */
		void iterate(final NodeModel node) {
			visitor.visit(node, NodeAttributeTableModel.getModel(node));
			for (final NodeModel child : node.getChildren()) {
				iterate(child);
			}
		}
	}

	private static interface IVisitor {
		void visit(final NodeModel node, NodeAttributeTableModel model);
	}

	private static class RegistryAttributeActor implements IActor {
		private final boolean manual;
		private final MapModel map;
		private final String name;
		private final AttributeRegistry registry;
		private final boolean visible;

		private RegistryAttributeActor(final String name, final boolean manual, final boolean visible,
		                               final AttributeRegistry registry, final MapModel map) {
			this.name = name;
			this.registry = registry;
			this.manual = manual;
			this.visible = visible;
			this.map = map;
		}

		@Override
		public void act() {
			final AttributeRegistryElement attributeRegistryElement = new AttributeRegistryElement(registry, name);
			attributeRegistryElement.setManual(manual);
			attributeRegistryElement.setVisibility(visible);
			final int index = registry.getElements().add(name, attributeRegistryElement);
			registry.getTableModel().fireTableRowsInserted(index, index);
			if (manual || visible) {
				final ModeController modeController = Controller.getCurrentModeController();
				modeController.getMapController().setSaved(map, false);
			}
		}

		@Override
		public String getDescription() {
			return "RegistryAttributeActor";
		}

		@Override
		public void undo() {
			registry.unregistry(name);
			if (manual) {
				final ModeController modeController = Controller.getCurrentModeController();
				modeController.getMapController().setSaved(map, false);
			}
		}
	}

	private static class RegistryAttributeValueActor implements IActor {
		private final AttributeRegistryElement element;
		private final Object newValue;
		private final boolean setManual;
		private final boolean wasManual;

		private RegistryAttributeValueActor(final AttributeRegistryElement element, final Object newValue, boolean setManual) {
			this.element = element;
			this.newValue = newValue;
			this.setManual = setManual;
			this.wasManual = element.isManual();
		}

		@Override
		public void act() {
			if (newValue != null){
				element.addValue(newValue);
				if(setManual)
					element.setManual(true);
			}
		}

		@Override
		public String getDescription() {
			return "RegistryAttributeValueActor";
		}

		@Override
		public void undo() {
			if (newValue != null){
				element.removeValue(newValue);
				if(setManual & ! wasManual)
					element.setManual(false);
			}
		}
	}

	private class RemoveAttributeActor implements IActor {
		final private InsertAttributeActor insertActor;

		private RemoveAttributeActor(NodeModel node, final NodeAttributeTableModel model, final int row) {
			final Attribute attribute = model.getAttribute(row);
			final String name = attribute.getName();
			final Object value = attribute.getValue();
			insertActor = new InsertAttributeActor(node, model, row, name, value);
		}

		@Override
		public void act() {
			insertActor.undo();
		}

		@Override
		public String getDescription() {
			return "RemoveAttributeActor";
		}

		@Override
		public void undo() {
			insertActor.act();
		}
	}

	private static class ReplaceAttributeValueActor implements IActor {
		private final String name;
		private final Object   newValue;
		private final Object oldValue;
		private final AttributeRegistry registry;

		private ReplaceAttributeValueActor(final AttributeRegistry registry, final String name, final Object oldValue,
		                                   final Object newValue) {
			this.registry = registry;
			this.name = name;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		@Override
		public void act() {
			registry.getElement(name).replaceValue(oldValue, newValue);
		}

		@Override
		public String getDescription() {
			return "ReplaceAttributeValueActor";
		}

		@Override
		public void undo() {
			registry.getElement(name).replaceValue(newValue, oldValue);
		}
	}

	private static class SetAttributeColumnWidthActor implements IActor {
		private final NodeModel node;
		private final int col;
		private final NodeAttributeTableModel model;
		private final Quantity<LengthUnits> oldWidth;
		private final Quantity<LengthUnits> width;

		private SetAttributeColumnWidthActor(NodeModel node, final int col, final Quantity<LengthUnits> oldWidth, final Quantity<LengthUnits> width,
		                                     final NodeAttributeTableModel model) {
			this.node = node;
			this.col = col;
			this.oldWidth = oldWidth;
			this.width = width;
			this.model = model;
		}

		@Override
		public void act() {
			model.getLayout().setColumnWidth(col, width);
		}

		@Override
		public String getDescription() {
			return "SetAttributeColumnWidthActor";
		}

		@Override
		public void undo() {
			model.getLayout().setColumnWidth(col, oldWidth);
		}
	}

	private static class SetAttributeNameActor implements IActor {
		private final NodeModel node;
		private final NodeAttributeTableModel model;
		private final String name;
		private final String oldName;
		private final int row;

		private SetAttributeNameActor(final NodeModel node, final NodeAttributeTableModel model, final String name, final String oldName,
		                              final int row) {
			this.node = node;
			this.model = model;
			this.name = name;
			this.oldName = oldName;
			this.row = row;
		}

		@Override
		public void act() {
			model.getAttribute(row).setName(name);
			model.fireTableCellUpdated(node, row, 0);
		}

		@Override
		public String getDescription() {
			return "setAttributeName";
		}

		@Override
		public void undo() {
			model.getAttribute(row).setName(oldName);
			model.fireTableCellUpdated(node, row, 0);
		}
	}

	private static class SetAttributeRestrictedActor implements IActor {
		private final int index;
		private final boolean isRestricted;
		private final AttributeRegistry registry;

		private SetAttributeRestrictedActor(final AttributeRegistry registry, final int index,
		                                    final boolean isRestricted) {
			this.registry = registry;
			this.index = index;
			this.isRestricted = isRestricted;
		}

		@Override
		public void act() {
			act(isRestricted);
		}

		public void act(final boolean isRestricted) {
			if (index == AttributeRegistry.GLOBAL) {
				registry.setRestricted(isRestricted);
			}
			else {
				registry.getElement(index).setRestriction(isRestricted);
			}
		}

		@Override
		public String getDescription() {
			return "SetAttributeRestrictedActor";
		}

		@Override
		public void undo() {
			act(!isRestricted);
		}
	}

	private static final class SetAttributeValueActor implements IActor {
		private final NodeAttributeTableModel model;
		private final Object newValue;
		private final Object oldValue;
		private final int row;
		private NodeModel node;

		private SetAttributeValueActor(NodeModel node, final NodeAttributeTableModel model, final int row, final Object newValue) {
			this.node = node;
			this.row = row;
			oldValue = model.getAttribute(row).getValue();
			this.newValue = newValue;
			this.model = model;
		}

		@Override
		public void act() {
			model.getAttribute(row).setValue(newValue);
			model.fireTableCellUpdated(node, row, 1);
		}

		@Override
		public String getDescription() {
			return "SetAttributeValue";
		}

		@Override
		public void undo() {
			model.getAttribute(row).setValue(oldValue);
			model.fireTableCellUpdated(node, row, 1);
		}
	}

	private static class SetAttributeVisibleActor implements IActor {
		private final AttributeRegistry attributeRegistry;
		private final int index;
		private final boolean isVisible;

		private SetAttributeVisibleActor(final AttributeRegistry attributeRegistry, final int index,
		                                 final boolean isVisible) {
			this.attributeRegistry = attributeRegistry;
			this.index = index;
			this.isVisible = isVisible;
		}

		@Override
		public void act() {
			act(isVisible);
		}

		private void act(final boolean isVisible) {
			attributeRegistry.getElement(index).setVisibility(isVisible);
			attributeRegistry.fireStateChanged();
		}

		@Override
		public String getDescription() {
			return "SetAttributeVisibleActor";
		}

		@Override
		public void undo() {
			act(!isVisible);
		}
	}

	private static class UnregistryAttributeActor implements IActor {
		final private RegistryAttributeActor registryActor;

		private UnregistryAttributeActor(final String name, final AttributeRegistry registry, final MapModel map) {
			registryActor = new RegistryAttributeActor(name, registry.getElement(name).isManual(), registry.getElement(
			    name).isVisible(), registry, map);
		}

		@Override
		public void act() {
			registryActor.undo();
		}

		@Override
		public String getDescription() {
			return "UnregistryAttributeActor";
		}

		@Override
		public void undo() {
			registryActor.act();
		}
	}

	private static class UnregistryAttributeValueActor implements IActor {
		final private RegistryAttributeValueActor registryActor;

		private UnregistryAttributeValueActor(final AttributeRegistryElement element, final Object newValue) {
			registryActor = new RegistryAttributeValueActor(element, newValue, element.isManual());
		}

		@Override
		public void act() {
			registryActor.undo();
		}

		@Override
		public String getDescription() {
			return "UnregistryAttributeValueActor";
		}

		@Override
		public void undo() {
			registryActor.act();
		}
	}

	InsertAttributeActor insertAttributeActor;

	public MAttributeController(final ModeController modeController) {
		super(modeController);
		createActions();
	}

	public int addAttribute(final NodeModel node, final Attribute pAttribute) {
		createAttributeTableModel(node);
		final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(node);
		final int rowCount = attributes.getRowCount();
		performInsertRow(node, attributes, rowCount, pAttribute.getName(), pAttribute.getValue());
		return rowCount;
	}

	@Override
	protected void registerAttributeClipboardController(final ModeController modeController) {
		final MClipboardControllers controllers = (MClipboardControllers) modeController.getExtension(ClipboardControllers.class);
		controllers.add(new MAttributeClipboardController(this));
	}


	/**
	 *
	 */
	private void createActions() {
		ModeController modeController = Controller.getCurrentModeController();
		modeController.addAction(new AssignAttributesAction());
		modeController.addAction(new ShowAttributeDialogAction());
		modeController.addAction(new CopyAttributes());
		modeController.addAction(new PasteAttributes());
		modeController.addAction(new AddStyleAttributes());
	}

	public int editAttribute(final NodeModel pNode, final String pName, final String pNewValue) {
		createAttributeTableModel(pNode);
		final Attribute newAttribute = new Attribute(pName, pNewValue);
		final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(pNode);
		for (int i = 0; i < attributes.getRowCount(); i++) {
			if (pName.equals(attributes.getAttribute(i).getName())) {
				if (pNewValue != null) {
					setAttribute(pNode, i, newAttribute);
				}
				else {
					removeAttribute(pNode, i);
				}
				return i;
			}
		}
		if (pNewValue == null) {
			return -1;
		}
		return addAttribute(pNode, newAttribute);
	}

	@Override
	public void performInsertRow(final NodeModel node, final NodeAttributeTableModel model, final int row, final String name, Object value) {
		final MapModel map = node.getMap();
		final AttributeRegistry attributes = AttributeRegistry.getRegistry(map);
		if (name.equals("")) {
			return;
		}
		try {
			final AttributeRegistryElement element = attributes.getElement(name);
			final int index = element.getValues().getIndexOf(value);
			if (index == -1) {
				if (element.isRestricted()) {
					value = element.getValues().firstElement().toString();
				}
				else {
					final IActor actor = new RegistryAttributeValueActor(element, value, false);
					Controller.getCurrentModeController().execute(actor, map);
				}
			}
		}
		catch (final NoSuchElementException ex) {
			final AttributeRegistry registry = AttributeRegistry.getRegistry(map);
			final IActor nameActor = new RegistryAttributeActor(name, false, false, registry, map);
			Controller.getCurrentModeController().execute(nameActor, map);
			final AttributeRegistryElement element = registry.getElement(name);
			final IActor valueActor = new RegistryAttributeValueActor(element, value, false);
			Controller.getCurrentModeController().execute(valueActor, map);
		}
		final Object newValue = value;
		final IActor actor = new InsertAttributeActor(node, model, row, name, newValue);
		Controller.getCurrentModeController().execute(actor, map);
	}

	@Override
	public void performRegistryAttribute(MapModel map, final String name) {
		if (name.equals("")) {
			return;
		}
		final AttributeRegistry attributeRegistry = AttributeRegistry.getRegistry(map);
		try {
			attributeRegistry.getElement(name);
		}
		catch (final NoSuchElementException ex) {
			final IActor actor = new RegistryAttributeActor(name, true, false, attributeRegistry, map);
			Controller.getCurrentModeController().execute(actor, map);
			return;
		}
	}

	@Override
	public void performRegistryAttributeValue(MapModel map, final String name, final String value, boolean manual) {
		if (name.equals("")) {
			return;
		}
		final AttributeRegistry attributeRegistry = AttributeRegistry.getRegistry(map);
		try {
			final AttributeRegistryElement element = attributeRegistry.getElement(name);
			if (element.getValues().contains(value)) {
				return;
			}
			final IActor actor = new RegistryAttributeValueActor(element, value, manual);
			Controller.getCurrentModeController().execute(actor, map);
			return;
		}
		catch (final NoSuchElementException ex) {
			final IActor nameActor = new RegistryAttributeActor(name, true, false, attributeRegistry, map);
			Controller.getCurrentModeController().execute(nameActor, map);
			final AttributeRegistryElement element = attributeRegistry.getElement(name);
			final IActor valueActor = new RegistryAttributeValueActor(element, value, false);
			Controller.getCurrentModeController().execute(valueActor, map);
		}
	}

	@Override
	public void performRegistrySubtreeAttributes(final NodeModel node) {
		final NodeAttributeTableModel nodeAttributeTableModel = NodeAttributeTableModel.getModel(node);
		for (int i = 0; i < nodeAttributeTableModel.getRowCount(); i++) {
			final String name = nodeAttributeTableModel.getValueAt(i, 0).toString();
			final String value = nodeAttributeTableModel.getValueAt(i, 1).toString();
			performRegistryAttributeValue(node.getMap(), name, value, false);
		}
		for (final NodeModel child : node.getChildren()) {
			performRegistrySubtreeAttributes(child);
		}
	}

	@Override
	public void performRemoveAttribute(MapModel map, final String name) {
		final IVisitor remover = new AttributeRemover(name);
		final Iterator iterator = new Iterator(remover);
		ModeController modeController = Controller.getCurrentModeController();
		final NodeModel root = modeController.getMapController().getRootNode();
		iterator.iterate(root);
		final AttributeRegistry attributeRegistry = AttributeRegistry.getRegistry(map);
		final IActor actor = new UnregistryAttributeActor(name, attributeRegistry, map);
		Controller.getCurrentModeController().execute(actor, map);
	}

	@Override
	public void performRemoveAttributeValue(MapModel map, final String name, final Object value) {
		final IVisitor remover = new AttributeValueRemover(name, value);
		final Iterator iterator = new Iterator(remover);
		ModeController modeController = Controller.getCurrentModeController();
		final NodeModel root = modeController.getMapController().getRootNode();
		iterator.iterate(root);
		final AttributeRegistry attributeRegistry = AttributeRegistry.getRegistry(map);
		final IActor unregistryActor = new UnregistryAttributeValueActor(attributeRegistry.getElement(name), value);
		Controller.getCurrentModeController().execute(unregistryActor, map);
	}

	@Override
	public Attribute performRemoveRow(final NodeModel node, final NodeAttributeTableModel model, final int row) {
		final Vector<Attribute> attributes = model.getAttributes();
		final Object o = attributes.elementAt(row);
		final IActor actor = new RemoveAttributeActor(node, model, row);
		Controller.getCurrentModeController().execute(actor, node.getMap());
		return (Attribute) o;
	}

	@Override
	public void performReplaceAtributeName(MapModel map, final String oldName, final String newName) {
		if (oldName.equals("") || newName.equals("") || oldName.equals(newName)) {
			return;
		}
		final AttributeRegistry registry = AttributeRegistry.getRegistry(map);
		final int iOld = registry.getElements().indexOf(oldName);
		final AttributeRegistryElement oldElement = registry.getElement(iOld);
		final SortedComboBoxModel values = oldElement.getValues();
		final IActor registryActor = new RegistryAttributeActor(newName, oldElement.isManual(), oldElement.isVisible(),
		    registry, map);
		Controller.getCurrentModeController().execute(registryActor, map);
		final AttributeRegistryElement newElement = registry.getElement(newName);
		for (int i = 0; i < values.getSize(); i++) {
			final IActor registryValueActor = new RegistryAttributeValueActor(newElement, values.getElementAt(i)
			    .toString(), false);
			Controller.getCurrentModeController().execute(registryValueActor, map);
		}
		final IVisitor replacer = new AttributeRenamer(oldName, newName);
		final Iterator iterator = new Iterator(replacer);
		ModeController modeController = Controller.getCurrentModeController();
		final NodeModel root = modeController.getMapController().getRootNode();
		iterator.iterate(root);
		final IActor unregistryActor = new UnregistryAttributeActor(oldName, registry, map);
		Controller.getCurrentModeController().execute(unregistryActor, map);
	}

	@Override
	public void performReplaceAttributeValue(MapModel map, final String name, final Object oldValue, final Object newValue) {
		final AttributeRegistry registry = AttributeRegistry.getRegistry(map);
		final IActor actor = new ReplaceAttributeValueActor(registry, name, oldValue, newValue);
		Controller.getCurrentModeController().execute(actor, map);
		final IVisitor replacer = new AttributeChanger(name, oldValue, newValue);
		final Iterator iterator = new Iterator(replacer);
		final NodeModel root = map.getRootNode();
		iterator.iterate(root);
	}

	@Override
	public void performSetColumnWidth(final NodeModel node, final NodeAttributeTableModel model, final int col, final Quantity<LengthUnits> width) {
		final Quantity<LengthUnits> oldWidth = model.getLayout().getColumnWidth(col);
		if (width.equals(oldWidth)) {
			return;
		}
		final IActor actor = new SetAttributeColumnWidthActor(node, col, oldWidth, width, model);
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}

	@Override
	public void performSetRestriction(MapModel map, final int index, final boolean isRestricted) {
		boolean currentValue;
		final AttributeRegistry registry = AttributeRegistry.getRegistry(map);
		if (index == AttributeRegistry.GLOBAL) {
			currentValue = registry.isRestricted();
		}
		else {
			currentValue = registry.getElement(index).isRestricted();
		}
		if (currentValue == isRestricted) {
			return;
		}
		final IActor actor = new SetAttributeRestrictedActor(registry, index, isRestricted);
		Controller.getCurrentModeController().execute(actor, map);
	}

	@Override
	public void performSetValueAt(final NodeModel node, final NodeAttributeTableModel model, final Object o, final int row, final int col) {
		final Attribute attribute = model.getAttribute(row);
		final MapModel map = node.getMap();
		final AttributeRegistry registry = AttributeRegistry.getRegistry(map);
		switch (col) {
			case 0: {
				final String name = o.toString().trim();
				final String oldName = attribute.getName();
				if (oldName.equals(name)) {
					return;
				}
				final IActor nameActor = new SetAttributeNameActor(node, model, name, oldName, row);
				Controller.getCurrentModeController().execute(nameActor, map);
				try {
					final AttributeRegistryElement element = registry.getElement(name);
					final String value = model.getValueAt(row, 1).toString();
					final int index = element.getValues().getIndexOf(value);
					if (index == -1) {
						final IActor valueActor = new SetAttributeValueActor(node, model, row, element.getValues().firstElement());
						Controller.getCurrentModeController().execute(valueActor, map);
					}
				}
				catch (final NoSuchElementException ex) {
					final IActor registryActor = new RegistryAttributeActor(name, false, false, registry, map);
					Controller.getCurrentModeController().execute(registryActor, map);
				}
				break;
			}
			case 1: {
				if (attribute.getValue().equals(o)) {
					return;
				}
				final IActor actor = new SetAttributeValueActor(node, model, row, o);
				Controller.getCurrentModeController().execute(actor, map);
				final String name = model.getValueAt(row, 0).toString();
				final AttributeRegistryElement element = registry.getElement(name);
				final int index = element.getValues().getIndexOf(o);
				if (index == -1) {
					final IActor registryActor = new RegistryAttributeValueActor(element, o, false);
					Controller.getCurrentModeController().execute(registryActor, map);
				}
				break;
			}
		}
	}

	@Override
	public void performSetVisibility(MapModel map, final int index, final boolean isVisible) {
		final AttributeRegistry attributeRegistry = AttributeRegistry.getRegistry(map);
		if (attributeRegistry.getElement(index).isVisible() == isVisible) {
			return;
		}
		final IActor actor = new SetAttributeVisibleActor(attributeRegistry, index, isVisible);
		Controller.getCurrentModeController().execute(actor, map);
	}

	public void removeAttribute(final NodeModel node, final int pPosition) {
		createAttributeTableModel(node);
		performRemoveRow(node, NodeAttributeTableModel.getModel(node), pPosition);
	}

	public void setAttribute(final NodeModel node, final int pPosition, final Attribute pAttribute) {
		createAttributeTableModel(node);
		final NodeAttributeTableModel model = NodeAttributeTableModel.getModel(node);
		performSetValueAt(node, model, pAttribute.getName(), pPosition, 0);
		performSetValueAt(node, model, pAttribute.getValue(), pPosition, 1);
	}

    public void copyAttributesToNode(NodeModel source, NodeModel target) {
        if (source == null)
            return;
        final NodeAttributeTableModel model = NodeAttributeTableModel.getModel(source);
        if (model.getRowCount() == 0)
            return;
        final int attributeTableLength = model.getAttributeTableLength();
        for(int i = 0; i < attributeTableLength; i++){
            final Attribute attribute = model.getAttribute(i);
            addAttribute(target, new Attribute(attribute.getName(), attribute.getValue()));
        }
    }
	@Override
	public boolean canEdit() {
	    return true;
    }
}
