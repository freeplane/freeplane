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
package org.freeplane.features.attribute;

import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;

import javax.swing.ComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.util.collection.IListModel;
import org.freeplane.core.util.collection.SortedComboBoxModel;
import org.freeplane.core.util.collection.SortedMapVector;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
public class AttributeRegistry implements IExtension {
	static public final int GLOBAL = -1;

	public static AttributeRegistry getRegistry(final MapModel map) {
		AttributeRegistry registry = (AttributeRegistry) map.getExtension(AttributeRegistry.class);
		if (registry == null) {
			final AttributeController attributeController = AttributeController.getController();
			registry = new AttributeRegistry(attributeController);
			map.addExtension(AttributeRegistry.class, registry);
			final NodeModel rootNode = map.getRootNode();
			if(rootNode != null)
				registry.registryAttributes(Controller.getCurrentModeController().getMapController(), rootNode);
		}
		return registry;
	}

	private AttributeController attributeController;
	private ChangeEvent attributesEvent;
	private String attributeViewType;
	private ChangeEvent changeEvent;
	protected SortedMapVector elements;
	protected boolean isAttributeLayoutChanged;
	private boolean isRestricted;
	private HashSet<IAttributesListener> attributeListeners = null;
	private HashSet<ChangeListener> changeListeners = null;
	private AttributeRegistryComboBoxColumnModel myComboBoxColumnModel = null;
	private AttributeRegistryTableModel myTableModel = null;
	private Boolean restrictionModel;
	protected int visibleElementsNumber;

	/**
	 *
	 */
	public AttributeRegistry() {
		super();
	}

	public AttributeRegistry(final AttributeController attributeController) {
		super();
		attributeListeners = new HashSet<IAttributesListener>();
		changeListeners = new HashSet<ChangeListener>();
		isAttributeLayoutChanged = false;
		this.attributeController = attributeController;
		visibleElementsNumber = 0;
		elements = new SortedMapVector();
		myTableModel = new AttributeRegistryTableModel(this);
		isRestricted = false;
		restrictionModel = Boolean.FALSE;
		attributeViewType = AttributeTableLayoutModel.SHOW_ALL;
	}

	public void addAttributesListener(final IAttributesListener l) {
		attributeListeners.add(l);
	}

	public void addChangeListener(final ChangeListener l) {
		changeListeners.add(l);
	}

	public void applyChanges() {
		if (isAttributeLayoutChanged == false) {
			return;
		}
		getAttributeController().performSetRestriction(AttributeRegistry.GLOBAL, restrictionModel.booleanValue());
		for (int i = 0; i < elements.size(); i++) {
			final AttributeRegistryElement element = getElement(i);
			getAttributeController().performSetVisibility(i, element.getVisibilityModel().booleanValue());
			getAttributeController().performSetRestriction(i, element.getRestriction().booleanValue());
		}
		isAttributeLayoutChanged = false;
	}

	public boolean containsElement(final String name) {
		return elements.containsKey(name);
	}

	public void decrementVisibleElementsNumber() {
		visibleElementsNumber--;
	}

	public boolean exist(final String attributeName, final Object element) {
		final int index = indexOf(attributeName);
		if (index == -1) {
			return false;
		}
		final SortedComboBoxModel values = getElement(index).getValues();
		for (int i = 0; i < values.getSize(); i++) {
			if (element.equals(values.getElementAt(i))) {
				return true;
			}
		}
		return false;
	}

	public void fireAttributeLayoutChanged() {
		fireStateChanged();
	}

	protected void fireAttributesChanged() {
		for (IAttributesListener l : attributeListeners) {
			if (attributesEvent == null) {
				attributesEvent = new ChangeEvent(this);
			}
			l.attributesChanged(changeEvent);
		}
	}

	public void fireStateChanged() {
		for (ChangeListener l : changeListeners) {
			if (changeEvent == null) {
				changeEvent = new ChangeEvent(this);
			}
			l.stateChanged(changeEvent);
		}
	}

	public AttributeController getAttributeController() {
		return attributeController;
	}

	public String getAttributeViewType() {
		return attributeViewType;
	}

	private AttributeRegistryComboBoxColumnModel getCombinedModel() {
		if (myComboBoxColumnModel == null) {
			myComboBoxColumnModel = new AttributeRegistryComboBoxColumnModel(this);
		}
		return myComboBoxColumnModel;
	}

	public ComboBoxModel getComboBoxModel() {
		return getCombinedModel();
	}

	public ComboBoxModel getDefaultComboBoxModel(final Comparable<?> attrName) {
		try {
			final AttributeRegistryElement elem = getElement(attrName);
			return elem.getValues();
		}
		catch (final NoSuchElementException ex) {
			return getComboBoxModel();
		}
	}

	public AttributeRegistryElement getElement(final Comparable<?> attrName) {
		final AttributeRegistryElement elem = (AttributeRegistryElement) elements.getValue(attrName);
		return elem;
	}

	public AttributeRegistryElement getElement(final int index) {
		return (AttributeRegistryElement) elements.getValue(index);
	}

	public SortedMapVector getElements() {
		return elements;
	}

	public Comparable<?> getKey(final int index) {
		return elements.getKey(index);
	}

	public IListModel getListBoxModel() {
		return getCombinedModel();
	}

	Boolean getRestriction(final int row) {
		if (row == AttributeRegistry.GLOBAL) {
			return restrictionModel;
		}
		else {
			return getElement(row).getRestriction();
		}
	}

	/**
	 */
	public AttributeRegistryTableModel getTableModel() {
		return myTableModel;
	}

	/**
	 */
	public IListModel getValues(final int row) {
		if (row == AttributeRegistry.GLOBAL) {
			return getListBoxModel();
		}
		return getElement(row).getValues();
	}

	public int getVisibleElementsNumber() {
		return visibleElementsNumber;
	}

	public void incrementVisibleElementsNumber() {
		visibleElementsNumber++;
	}

	/**
	 */
	public int indexOf(final String string) {
		return elements.indexOf(string);
	}

	/**
	 */
	public boolean isRestricted() {
		return isRestricted;
	}

	public boolean isRestricted(final String s) {
		return getRestriction(indexOf(s)).booleanValue();
	}

	public void registry(final Attribute newAttribute) {
		final String name = newAttribute.getName();
		if (name.equals("")) {
			return;
		}
		final Object value = newAttribute.getValue();
		try {
			final AttributeRegistryElement elem = getElement(name);
			elem.addValue(value);
		}
		catch (final NoSuchElementException ex) {
			final AttributeRegistryElement attributeRegistryElement = new AttributeRegistryElement(this, name);
			attributeRegistryElement.addValue(value);
			final int index = getElements().add(name, attributeRegistryElement);
			getTableModel().fireTableRowsInserted(index, index);
		};
		fireAttributesChanged();
	}

	public void registry(final String name) {
		final AttributeRegistryElement attributeRegistryElement = new AttributeRegistryElement(this, name);
		final int index = getElements().add(name, attributeRegistryElement);
		getTableModel().fireTableRowsInserted(index, index);
	}

	private void registryAttributes(final MapController mapController, final NodeModel node) {
		final NodeAttributeTableModel model = NodeAttributeTableModel.getModel(node);
		if (model == null) {
			return;
		}
		for (int i = 0; i < model.getRowCount(); i++) {
			registry(model.getAttribute(i));
		}
		for (final NodeModel child : mapController.childrenUnfolded(node)) {
			registryAttributes(mapController, child);
		}
	}

	/**
	 */
	void removeAtribute(final Object o) {
		getAttributeController().performRemoveAttribute(o.toString());
	}

	public void removeAttributesListener(final IAttributesListener l) {
		attributeListeners.remove(l);
	}

	public void removeChangeListener(final ChangeListener l) {
		changeListeners.remove(l);
	}

	/**
	 */
	public void resetChanges() {
		if (isAttributeLayoutChanged == false) {
			return;
		}
		restrictionModel = Boolean.valueOf(isRestricted);
		for (int i = 0; i < elements.size(); i++) {
			final AttributeRegistryElement element = getElement(i);
			element.setVisibilityModel(Boolean.valueOf(element.isVisible()));
			element.setRestrictionModel(Boolean.valueOf(element.isRestricted()));
		}
		isAttributeLayoutChanged = false;
	}

	public void setAttributeLayoutChanged() {
		isAttributeLayoutChanged = true;
	}

	public void setAttributeViewType(final String attributeViewType) {
		this.attributeViewType = attributeViewType;
		fireStateChanged();
	}

	/**
	 */
	public void setRestricted(final boolean b) {
		isRestricted = b;
		restrictionModel = Boolean.valueOf(isRestricted);
		fireAttributesChanged();
	}

	/**
	 */
	private void setRestricted(final int row, final boolean b) {
		getElement(row).setRestriction(b);
	}

	public void setRestricted(final String s, final boolean b) {
		setRestricted(indexOf(s), b);
	}

	/**
	 */
	public void setRestrictionModel(final int row, final Boolean value) {
		if (row == AttributeRegistry.GLOBAL) {
			restrictionModel = value;
		}
		else {
			getElement(row).setRestrictionModel(value);
		}
		setAttributeLayoutChanged();
		myTableModel.fireRestrictionsUpdated(row);
	}

	public void setVisibilityModel(final int row, final Boolean visible) {
		final AttributeRegistryElement element = getElement(row);
		if (!element.getVisibilityModel().equals(visible)) {
			element.setVisibilityModel(visible);
			setAttributeLayoutChanged();
			myTableModel.fireVisibilityUpdated(row);
		}
	}

	public int size() {
		return elements.size();
	}

	public void unregistry(final String name) {
		final int index = elements.indexOf(name);
		if (getElement(index).isVisible()) {
			decrementVisibleElementsNumber();
		}
		elements.remove(index);
		getTableModel().fireTableRowsDeleted(index, index);
		fireAttributesChanged();
	}

	/**
	 * @throws IOException
	 */
	public void write(final ITreeWriter writer) throws IOException {
		final XMLElement attributeRegistry = new XMLElement();
		boolean toBeSaved = false;
		if (isRestricted()) {
			attributeRegistry.setAttribute("RESTRICTED", "true");
			toBeSaved = true;
		}
		if (!attributeViewType.equals(AttributeTableLayoutModel.SHOW_ALL)) {
			attributeRegistry.setAttribute("SHOW_ATTRIBUTES", attributeViewType);
			toBeSaved = true;
		}
		for (int i = 0; i < size(); i++) {
			final AttributeRegistryElement element = getElement(i);
			if (element.isRestricted() || element.isVisible() || element.isManual()) {
				final XMLElement attributeData = element.save();
				attributeRegistry.addChild(attributeData);
				toBeSaved = true;
			}
		}
		if (toBeSaved) {
			attributeRegistry.setName(AttributeBuilder.XML_NODE_ATTRIBUTE_REGISTRY);
			writer.addElement(this, attributeRegistry);
		}
	}
}
