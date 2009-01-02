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
package org.freeplane.core.filter;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.filter.condition.ConditionFactory;
import org.freeplane.core.filter.condition.DefaultConditionRenderer;
import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.filter.condition.NoFilteringCondition;
import org.freeplane.core.frame.IMapViewChangeListener;
import org.freeplane.core.io.XMLElement;
import org.freeplane.core.map.MapModel;
import org.freeplane.core.map.MindIcon;
import org.freeplane.core.view.IMapView;
import org.freeplane.map.note.NodeNoteBase;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.n3.nanoxml.XMLParserFactory;
import org.freeplane.n3.nanoxml.XMLWriter;

/**
 * @author Dimitry Polivaev
 */
public class FilterController implements IMapViewChangeListener {
	static final String FREEMIND_FILTER_EXTENSION_WITHOUT_DOT = "mmfilter";
	private ConditionFactory conditionFactory;
	private DefaultConditionRenderer conditionRenderer = null;
	private DefaultComboBoxModel filterConditionModel;
	private FilterToolbar filterToolbar;
	private IFilter inactiveFilter;
	private MapModel map;

	public FilterController() {
		Controller.getController().getMapViewManager().addMapViewChangeListener(this);
		Controller.getController().addAction("showFilterToolbarAction",
		    new ShowFilterToolbarAction());
	}

	public void afterMapClose(final IMapView pOldMapView) {
	}

	public void afterMapViewChange(final IMapView oldMapView, final IMapView newMapView) {
		final MapModel newMap = newMapView != null ? newMapView.getModel() : null;
		final FilterComposerDialog fd = getFilterToolbar().getFilterDialog();
		if (fd != null) {
			fd.mapChanged(newMap);
		}
		map = newMap;
		getFilterToolbar().mapChanged(newMap);
	}

	public void beforeMapViewChange(final IMapView oldMapView, final IMapView newMapView) {
	}

	private IFilter createTransparentFilter() {
		if (inactiveFilter == null) {
			inactiveFilter = new DefaultFilter(NoFilteringCondition.createCondition(), true, false);
		}
		return inactiveFilter;
	}

	public ConditionFactory getConditionFactory() {
		if (conditionFactory == null) {
			conditionFactory = new ConditionFactory();
		}
		return conditionFactory;
	}

	DefaultConditionRenderer getConditionRenderer() {
		if (conditionRenderer == null) {
			conditionRenderer = new DefaultConditionRenderer();
		}
		return conditionRenderer;
	}

	public DefaultComboBoxModel getFilterConditionModel() {
		return filterConditionModel;
	}

	/**
	 */
	public FilterToolbar getFilterToolbar() {
		if (filterToolbar == null) {
			filterToolbar = new FilterToolbar();
			filterConditionModel = (DefaultComboBoxModel) filterToolbar.getFilterConditionModel();
			MindIcon.factory("AttributeExist", new ImageIcon(Controller.getResourceController()
			    .getResource("images/showAttributes.gif")));
			MindIcon.factory(NodeNoteBase.NODE_NOTE_ICON, new ImageIcon(Controller
			    .getResourceController().getResource("images/knotes.png")));
			MindIcon.factory("encrypted");
			MindIcon.factory("decrypted");
			filterToolbar.initConditions();
		}
		return filterToolbar;
	}

	/**
	 */
	public MapModel getMap() {
		return map;
	}

	public boolean isMapViewChangeAllowed(final IMapView oldMapView, final IMapView newMapView) {
		return true;
	}

	void loadConditions(final DefaultComboBoxModel filterConditionModel,
	                    final String pathToFilterFile) throws IOException {
		filterConditionModel.removeAllElements();
		try {
			final IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			final IXMLReader reader = new StdXMLReader(new FileInputStream(pathToFilterFile));
			parser.setReader(reader);
			final XMLElement loader = (XMLElement) parser.parse();
			final Vector conditions = loader.getChildren();
			for (int i = 0; i < conditions.size(); i++) {
				filterConditionModel.addElement(getConditionFactory().loadCondition(
				    (XMLElement) conditions.get(i)));
			}
		}
		catch (final XMLException e) {
			e.printStackTrace();
		}
	}

	void refreshMap() {
		Controller.getController();
		Controller.getModeController().getMapController().refreshMap();
	}

	public void saveConditions() {
		if (filterToolbar != null) {
			filterToolbar.saveConditions();
		}
	}

	void saveConditions(final DefaultComboBoxModel filterConditionModel,
	                    final String pathToFilterFile) throws IOException {
		final XMLElement saver = new XMLElement();
		saver.setName("filter_conditions");
		final Writer writer = new FileWriter(pathToFilterFile);
		for (int i = 0; i < filterConditionModel.getSize(); i++) {
			final ICondition cond = (ICondition) filterConditionModel.getElementAt(i);
			cond.toXml(saver);
		}
		final XMLWriter xmlWriter = new XMLWriter(writer);
		xmlWriter.write(saver);
		writer.close();
	}

	public void setFilterConditionModel(final DefaultComboBoxModel filterConditionModel) {
		this.filterConditionModel = filterConditionModel;
		filterToolbar.setFilterConditionModel(filterConditionModel);
	}

	/**
	 */
	public void showFilterToolbar(final boolean show) {
		if (show == getFilterToolbar().isVisible()) {
			return;
		}
		getFilterToolbar().setVisible(show);
		final IFilter filter = getMap().getFilter();
		if (show) {
			filter.applyFilter();
		}
		else {
			createTransparentFilter().applyFilter();
		}
		refreshMap();
	}
}
