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
package org.freeplane.map.attribute.mindmapnode;

import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.freeplane.controller.Freeplane;
import org.freeplane.map.attribute.Attribute;
import org.freeplane.map.attribute.AttributeController;
import org.freeplane.map.attribute.AttributeRegistry;
import org.freeplane.map.attribute.AttributeRegistryElement;
import org.freeplane.map.attribute.IAttributeController;
import org.freeplane.map.attribute.NodeAttributeTableModel;
import org.freeplane.map.attribute.view.AttributePopupMenu;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.service.filter.util.SortedComboBoxModel;

import deprecated.freemind.modes.mindmapmode.actions.undo.ActionPair;

public class MAttributeController extends AttributeController implements
        IAttributeController {
	private class AttributeChanger implements IVisitor {
		final private Object name;
		final private Object newValue;
		final private Object oldValue;

		public AttributeChanger(final Object name, final Object oldValue,
		                        final Object newValue) {
			super();
			this.name = name;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * freemind.modes.attributes.AttributeRegistry.Visitor#visit(freemind
		 * .modes.attributes.ConcreteAttributeTableModel)
		 */
		public void visit(final NodeAttributeTableModel model) {
			for (int i = 0; i < model.getRowCount(); i++) {
				if (model.getName(i).equals(name)
				        && model.getValue(i).equals(oldValue)) {
					final ActionPair setAttributeValueActionPair = setAttributeValueActor
					    .createActionPair(model, i, newValue.toString());
					modeController.getActionFactory().executeAction(
					    setAttributeValueActionPair);
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
		 * freemind.modes.attributes.AttributeRegistry.Visitor#visit(freemind
		 * .modes.attributes.ConcreteAttributeTableModel)
		 */
		public void visit(final NodeAttributeTableModel model) {
			for (int i = 0; i < model.getRowCount(); i++) {
				if (model.getName(i).equals(name)) {
					final ActionPair removeAttributeActionPair = removeAttributeActor
					    .createActionPair(model, i);
					modeController.getActionFactory().executeAction(
					    removeAttributeActionPair);
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
		 * freemind.modes.attributes.AttributeRegistry.Visitor#visit(freemind
		 * .modes.attributes.ConcreteAttributeTableModel)
		 */
		public void visit(final NodeAttributeTableModel model) {
			for (int i = 0; i < model.getRowCount(); i++) {
				if (model.getName(i).equals(oldName)) {
					final ActionPair setAttributeNameActionPair = setAttributeNameActor
					    .createActionPair(model, i, newName.toString());
					modeController.getActionFactory().executeAction(
					    setAttributeNameActionPair);
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
		 * freemind.modes.attributes.AttributeRegistry.Visitor#visit(freemind
		 * .modes.attributes.ConcreteAttributeTableModel)
		 */
		public void visit(final NodeAttributeTableModel model) {
			for (int i = 0; i < model.getRowCount(); i++) {
				if (model.getName(i).equals(name)
				        && model.getValue(i).equals(value)) {
					final ActionPair removeAttributeActionPair = removeAttributeActor
					    .createActionPair(model, i);
					modeController.getActionFactory().executeAction(
					    removeAttributeActionPair);
				}
			}
		}
	}

	private static class Iterator {
		final private IVisitor visitor;

		Iterator(final IVisitor v) {
			visitor = v;
		}

		/**
		 */
		void iterate(final NodeModel node) {
			visitor.visit(node.getAttributes());
			final ListIterator iterator = node.getModeController()
			    .getMapController().childrenUnfolded(node);
			while (iterator.hasNext()) {
				final NodeModel child = (NodeModel) iterator.next();
				iterate(child);
			}
		}
	}

	private static interface IVisitor {
		void visit(NodeAttributeTableModel model);
	}

	static private boolean actionsCreated = false;
	InsertAttributeActor insertAttributeActor;
	final private MModeController modeController;
	RegistryAttributeActor registryAttributeActor;
	RegistryAttributeValueActor registryAttributeValueActor;
	RemoveAttributeActor removeAttributeActor;
	ReplaceAttributeValueActor replaceAttributeValueActor;
	SetAttributeColumnWidthActor setAttributeColumnWidthActor;
	SetAttributeFontSizeActor setAttributeFontSizeActor;
	SetAttributeNameActor setAttributeNameActor;
	SetAttributeRestrictedActor setAttributeRestrictedActor;
	SetAttributeValueActor setAttributeValueActor;
	SetAttributeVisibleActor setAttributeVisibleActor;
	UnregistryAttributeActor unregistryAttributeActor;
	UnregistryAttributeValueActor unregistryAttributeValueActor;

	public MAttributeController(final MModeController modeController) {
		super(modeController);
		this.modeController = modeController;
		setAttributeNameActor = new SetAttributeNameActor(modeController);
		setAttributeValueActor = new SetAttributeValueActor(modeController);
		removeAttributeActor = new RemoveAttributeActor(modeController);
		insertAttributeActor = new InsertAttributeActor(modeController);
		setAttributeColumnWidthActor = new SetAttributeColumnWidthActor(
		    modeController);
		registryAttributeActor = new RegistryAttributeActor(modeController);
		unregistryAttributeActor = new UnregistryAttributeActor(modeController);
		registryAttributeValueActor = new RegistryAttributeValueActor(
		    modeController);
		replaceAttributeValueActor = new ReplaceAttributeValueActor(
		    modeController);
		unregistryAttributeValueActor = new UnregistryAttributeValueActor(
		    modeController);
		setAttributeFontSizeActor = new SetAttributeFontSizeActor(
		    modeController);
		setAttributeVisibleActor = new SetAttributeVisibleActor(modeController);
		setAttributeRestrictedActor = new SetAttributeRestrictedActor(
		    modeController);
		createActions();
	}

	public int addAttribute(final NodeModel node, final Attribute pAttribute) {
		node.createAttributeTableModel();
		final NodeAttributeTableModel attributes = node.getAttributes();
		final int rowCount = attributes.getRowCount();
		performInsertRow(attributes, rowCount, pAttribute.getName(), pAttribute
		    .getValue());
		return rowCount;
	}

	/**
	 *
	 */
	private void createActions() {
		if (!actionsCreated) {
			actionsCreated = true;
			Freeplane.getController().addAction("editAttributes",
			    new EditAttributesAction(modeController));
			Freeplane.getController().addAction("assignAttributes",
			    new AssignAttributesAction(modeController));
		}
	}

	public int editAttribute(final NodeModel pNode, final String pName,
	                         final String pNewValue) {
		pNode.createAttributeTableModel();
		final Attribute newAttribute = new Attribute(pName, pNewValue);
		final NodeAttributeTableModel attributes = pNode.getAttributes();
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

	private void endTransaction(final String name) {
		modeController.getActionFactory().endTransaction(
		    this.getClass().getName() + "." + name);
	}

	private AttributeRegistry getAttributeRegistry() {
		return Freeplane.getController().getMap().getRegistry().getAttributes();
	}

	public AttributePopupMenu getAttributeTablePopupMenu() {
		return new AttributePopupMenu();
	}

	public void performInsertRow(final NodeAttributeTableModel model,
	                             final int row, final String name, String value) {
		startTransaction("performInsertRow");
		final AttributeRegistry attributes = getAttributeRegistry();
		if (name.equals("")) {
			return;
		}
		try {
			final AttributeRegistryElement element = attributes
			    .getElement(name);
			final int index = element.getValues().getIndexOf(value);
			if (index == -1) {
				if (element.isRestricted()) {
					value = element.getValues().firstElement().toString();
				}
				else {
					final ActionPair registryNewAttributeActionPair = registryAttributeValueActor
					    .createActionPair(name, value);
					modeController.getActionFactory().executeAction(
					    registryNewAttributeActionPair);
				}
			}
		}
		catch (final NoSuchElementException ex) {
			final ActionPair registryAttributeActionPair = registryAttributeActor
			    .createActionPair(name);
			modeController.getActionFactory().executeAction(
			    registryAttributeActionPair);
			final ActionPair registryAttributeValueActionPair = registryAttributeValueActor
			    .createActionPair(name, value);
			modeController.getActionFactory().executeAction(
			    registryAttributeValueActionPair);
		}
		final ActionPair insertAttributeActionPair = insertAttributeActor
		    .createActionPair(model, row, name, value);
		modeController.getActionFactory().executeAction(
		    insertAttributeActionPair);
		endTransaction("performInsertRow");
	}

	public void performRegistryAttribute(final String name) {
		if (name.equals("")) {
			return;
		}
		try {
			getAttributeRegistry().getElement(name);
		}
		catch (final NoSuchElementException ex) {
			startTransaction("performRegistryAttribute");
			final ActionPair registryNewAttributeActionPair = registryAttributeActor
			    .createActionPair(name);
			modeController.getActionFactory().executeAction(
			    registryNewAttributeActionPair);
			endTransaction("performRegistryAttribute");
			return;
		}
	}

	public void performRegistryAttributeValue(final String name,
	                                          final String value) {
		if (name.equals("")) {
			return;
		}
		try {
			final AttributeRegistryElement element = getAttributeRegistry()
			    .getElement(name);
			if (element.getValues().contains(value)) {
				return;
			}
			startTransaction("performRegistryAttributeValue");
			final ActionPair registryNewAttributeActionPair = registryAttributeValueActor
			    .createActionPair(name, value);
			modeController.getActionFactory().executeAction(
			    registryNewAttributeActionPair);
			endTransaction("performRegistryAttributeValue");
			return;
		}
		catch (final NoSuchElementException ex) {
			startTransaction("performRegistryAttributeValue");
			final ActionPair registryAttributeActionPair = registryAttributeActor
			    .createActionPair(name);
			modeController.getActionFactory().executeAction(
			    registryAttributeActionPair);
			final ActionPair registryAttributeValueActionPair = registryAttributeValueActor
			    .createActionPair(name, value);
			modeController.getActionFactory().executeAction(
			    registryAttributeValueActionPair);
			endTransaction("performRegistryAttributeValue");
			return;
		}
	}

	public void performRegistrySubtreeAttributes(final NodeModel node) {
		for (int i = 0; i < node.getAttributes().getRowCount(); i++) {
			final String name = node.getAttributes().getValueAt(i, 0)
			    .toString();
			final String value = node.getAttributes().getValueAt(i, 1)
			    .toString();
			performRegistryAttributeValue(name, value);
		}
		for (final ListIterator e = node.getModeController().getMapController()
		    .childrenUnfolded(node); e.hasNext();) {
			final NodeModel child = (NodeModel) e.next();
			performRegistrySubtreeAttributes(child);
		}
	}

	public void performRemoveAttribute(final String name) {
		startTransaction("performReplaceAtributeName");
		final ActionPair unregistryOldAttributeActionPair = unregistryAttributeActor
		    .createActionPair(name);
		modeController.getActionFactory().executeAction(
		    unregistryOldAttributeActionPair);
		final IVisitor remover = new AttributeRemover(name);
		final Iterator iterator = new Iterator(remover);
		final NodeModel root = modeController.getMapController().getRootNode();
		iterator.iterate(root);
		endTransaction("performReplaceAtributeName");
	}

	public void performRemoveAttributeValue(final String name,
	                                        final String value) {
		startTransaction("performRemoveAttributeValue");
		final ActionPair removeAttributeActionPair = unregistryAttributeValueActor
		    .createActionPair(name, value);
		modeController.getActionFactory().executeAction(
		    removeAttributeActionPair);
		final IVisitor remover = new AttributeValueRemover(name, value);
		final Iterator iterator = new Iterator(remover);
		final NodeModel root = modeController.getMapController().getRootNode();
		iterator.iterate(root);
		endTransaction("performRemoveAttributeValue");
	}

	public void performRemoveRow(final NodeAttributeTableModel model,
	                             final int row) {
		startTransaction("performRemoveRow");
		final ActionPair removeAttributeActionPair = removeAttributeActor
		    .createActionPair(model, row);
		modeController.getActionFactory().executeAction(
		    removeAttributeActionPair);
		endTransaction("performRemoveRow");
	}

	public void performReplaceAtributeName(final String oldName,
	                                       final String newName) {
		if (oldName.equals("") || newName.equals("") || oldName.equals(newName)) {
			return;
		}
		startTransaction("performReplaceAtributeName");
		final AttributeRegistry registry = getAttributeRegistry();
		final int iOld = registry.getElements().indexOf(oldName);
		final AttributeRegistryElement oldElement = registry.getElement(iOld);
		final SortedComboBoxModel values = oldElement.getValues();
		final ActionPair registryNewAttributeActionPair = registryAttributeActor
		    .createActionPair(newName);
		modeController.getActionFactory().executeAction(
		    registryNewAttributeActionPair);
		for (int i = 0; i < values.getSize(); i++) {
			final ActionPair registryNewAttributeValueActionPair = registryAttributeValueActor
			    .createActionPair(newName, values.getElementAt(i).toString());
			modeController.getActionFactory().executeAction(
			    registryNewAttributeValueActionPair);
		}
		final IVisitor replacer = new AttributeRenamer(oldName, newName);
		final Iterator iterator = new Iterator(replacer);
		final NodeModel root = modeController.getMapController().getRootNode();
		iterator.iterate(root);
		final ActionPair unregistryOldAttributeActionPair = unregistryAttributeActor
		    .createActionPair(oldName);
		modeController.getActionFactory().executeAction(
		    unregistryOldAttributeActionPair);
		endTransaction("performReplaceAtributeName");
	}

	public void performReplaceAttributeValue(final String name,
	                                         final String oldValue,
	                                         final String newValue) {
		startTransaction("performReplaceAttributeValue");
		final ActionPair replaceAttributeActionPair = replaceAttributeValueActor
		    .createActionPair(name, oldValue, newValue);
		modeController.getActionFactory().executeAction(
		    replaceAttributeActionPair);
		final IVisitor replacer = new AttributeChanger(name, oldValue, newValue);
		final Iterator iterator = new Iterator(replacer);
		final NodeModel root = modeController.getMapController().getRootNode();
		iterator.iterate(root);
		endTransaction("performReplaceAttributeValue");
	}

	public void performSetColumnWidth(final NodeAttributeTableModel model,
	                                  final int col, final int width) {
		if (width == model.getLayout().getColumnWidth(col)) {
			return;
		}
		startTransaction("performSetColumnWidth");
		final ActionPair setAttributeColumnWidthActionPair = setAttributeColumnWidthActor
		    .createActionPair(model, col, width);
		modeController.getActionFactory().executeAction(
		    setAttributeColumnWidthActionPair);
		endTransaction("performSetColumnWidth");
	}

	public void performSetFontSize(final AttributeRegistry registry,
	                               final int size) {
		if (size == registry.getFontSize()) {
			return;
		}
		startTransaction("performSetFontSize");
		final ActionPair setFontSizeActionPair = setAttributeFontSizeActor
		    .createActionPair(size);
		modeController.getActionFactory().executeAction(setFontSizeActionPair);
		endTransaction("performSetFontSize");
	}

	public void performSetRestriction(final int index,
	                                  final boolean isRestricted) {
		boolean currentValue;
		if (index == AttributeRegistry.GLOBAL) {
			currentValue = getAttributeRegistry().isRestricted();
		}
		else {
			currentValue = getAttributeRegistry().getElement(index)
			    .isRestricted();
		}
		if (currentValue == isRestricted) {
			return;
		}
		startTransaction("performSetRestriction");
		final ActionPair setRestrictionActionPair = setAttributeRestrictedActor
		    .createActionPair(index, isRestricted);
		modeController.getActionFactory().executeAction(
		    setRestrictionActionPair);
		endTransaction("performSetRestriction");
	}

	public void performSetValueAt(final NodeAttributeTableModel model,
	                              final Object o, final int row, final int col) {
		startTransaction("performSetValueAt");
		final Attribute attribute = model.getAttribute(row);
		final AttributeRegistry attributes = getAttributeRegistry();
		switch (col) {
			case 0: {
				final String name = o.toString().trim();
				if (attribute.getName().equals(name)) {
					return;
				}
				final ActionPair setAttributeNameActionPair = setAttributeNameActor
				    .createActionPair(model, row, name);
				modeController.getActionFactory().executeAction(
				    setAttributeNameActionPair);
				try {
					final AttributeRegistryElement element = attributes
					    .getElement(name);
					final String value = model.getValueAt(row, 1).toString();
					final int index = element.getValues().getIndexOf(value);
					if (index == -1) {
						final ActionPair setAttributeValueActionPair = setAttributeValueActor
						    .createActionPair(model, row, element.getValues()
						        .firstElement().toString());
						modeController.getActionFactory().executeAction(
						    setAttributeValueActionPair);
					}
				}
				catch (final NoSuchElementException ex) {
					final ActionPair registryAttributeActionPair = registryAttributeActor
					    .createActionPair(name);
					modeController.getActionFactory().executeAction(
					    registryAttributeActionPair);
					final ActionPair setAttributeValueActionPair = setAttributeValueActor
					    .createActionPair(model, row, "");
					modeController.getActionFactory().executeAction(
					    setAttributeValueActionPair);
				}
				break;
			}
			case 1: {
				final String value = o.toString().trim();
				if (attribute.getValue().equals(value)) {
					return;
				}
				final ActionPair setValueActionPair = setAttributeValueActor
				    .createActionPair(model, row, value);
				modeController.getActionFactory().executeAction(
				    setValueActionPair);
				final String name = model.getValueAt(row, 0).toString();
				final AttributeRegistryElement element = attributes
				    .getElement(name);
				final int index = element.getValues().getIndexOf(value);
				if (index == -1) {
					final ActionPair registryAttributeValueActionPair = registryAttributeValueActor
					    .createActionPair(name, value);
					modeController.getActionFactory().executeAction(
					    registryAttributeValueActionPair);
				}
				break;
			}
		}
		endTransaction("performSetValueAt");
	}

	public void performSetVisibility(final int index, final boolean isVisible) {
		if (getAttributeRegistry().getElement(index).isVisible() == isVisible) {
			return;
		}
		startTransaction("performSetVisibility");
		final ActionPair setVisibilityActionPair = setAttributeVisibleActor
		    .createActionPair(index, isVisible);
		modeController.getActionFactory()
		    .executeAction(setVisibilityActionPair);
		endTransaction("performSetVisibility");
	}

	public void removeAttribute(final NodeModel pNode, final int pPosition) {
		pNode.createAttributeTableModel();
		performRemoveRow(pNode.getAttributes(), pPosition);
	}

	public void setAttribute(final NodeModel pNode, final int pPosition,
	                         final Attribute pAttribute) {
		pNode.createAttributeTableModel();
		pNode.getAttributes().setValueAt(pAttribute.getName(), pPosition, 0);
		pNode.getAttributes().setValueAt(pAttribute.getValue(), pPosition, 1);
	}

	private void startTransaction(final String name) {
		modeController.getActionFactory().startTransaction(
		    this.getClass().getName() + "." + name);
	}
}
