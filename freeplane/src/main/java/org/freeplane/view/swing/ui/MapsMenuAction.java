/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.view.swing.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.view.swing.map.MapView;

@SuppressWarnings("serial")
@SelectableAction
class MapsMenuAction extends AFreeplaneAction implements PropertyChangeListener {
    private static final String MAPS_MENU_ACTION_DOT = "MapsMenuAction.";
    private final MapView mapView;

    public MapsMenuAction(MapView mapView) {
        super(MAPS_MENU_ACTION_DOT + mapView.getName(), label(mapView), null);
        this.mapView = mapView;
        setSelected(mapView.isSelected());
        mapView.addPropertyChangeListener(MapView.TITLE_REFRESH_PROPERTY, this);
        mapView.addPropertyChangeListener(SelectableAction.SELECTION_PROPERTY, this);
    }

    @Override
    public String getRawText() {
        return label(mapView);
    }

    private static String label(MapView mapView) {
        String name = mapView.getName();
        String resolvedTitle = MapDisplayNameResolver.resolveBaseLabel(mapView);
        if (!mapView.getMap().isSaved() && !mapView.getMap().isReadOnly())
            resolvedTitle += " *";
        return name + (name.equals(resolvedTitle) ? "" : " : " + resolvedTitle);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (MapView.TITLE_REFRESH_PROPERTY.equals(event.getPropertyName())) {
            Action presentation = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    throw new UnsupportedOperationException();
                }
            };
            LabelAndMnemonicSetter.setLabelAndMnemonic(presentation, getRawText());
            for (String key : new String[] { Action.NAME, Action.MNEMONIC_KEY, Action.DISPLAYED_MNEMONIC_INDEX_KEY }) {
                Object value = presentation.getValue(key);
                if (!Objects.equals(getValue(key), value))
                    putValue(key, value);
            }
        }
        else if (SelectableAction.SELECTION_PROPERTY.equals(event.getPropertyName())) {
            setSelected(mapView.isSelected());
        }
    }

    void dispose() {
        mapView.removePropertyChangeListener(MapView.TITLE_REFRESH_PROPERTY, this);
        mapView.removePropertyChangeListener(SelectableAction.SELECTION_PROPERTY, this);
    }

    @Override
    public void actionPerformed(ActionEvent menuEvent) {
        UITools.executeWhenNodeHasFocus(() -> {
            try {
                IMapViewManager manager = Controller.getCurrentController().getMapViewManager();
                if (!manager.getMapViews().contains(mapView))
                    throw new IllegalArgumentException("Map mapView " + mapView.getName() + " not found.");
                manager.changeToMapView(mapView);
            }
            catch (IllegalArgumentException ex) {
                LogUtils.warn(ex);
            }
        });
    }
}
