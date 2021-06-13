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
package org.freeplane.features.mode;

import java.awt.event.ActionEvent;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.view.swing.map.MapView;

class FreeScrollAction extends AFreeplaneAction {
    private static final long serialVersionUID = 1L;
    private static final String WHEEL_VELOCITY = "wheel_velocity";

    public enum Direction {
        LEFT {
            @Override
            void scroll(MapView mapView, int wheelVelocity) {
                mapView.scrollBy(wheelVelocity, 0);
            }
        },
        UP {
            @Override
            void scroll(MapView mapView, int wheelVelocity) {
                mapView.scrollBy(0, wheelVelocity);
            }
        },
        RIGHT {
            @Override
            void scroll(MapView mapView, int wheelVelocity) {
                mapView.scrollBy(-wheelVelocity, 0);
            }
        },
        DOWN {
            @Override
            void scroll(MapView mapView, int wheelVelocity) {
                mapView.scrollBy(0, -wheelVelocity);
            }
        };

        abstract void scroll(MapView mapView, int wheelVelocity);
    }

    private final Direction direction;

    public FreeScrollAction(final Direction direction) {
        super("FreeScrollAction." + direction.name());
        this.direction = direction;
    }

    public void actionPerformed(final ActionEvent e) {
        final MapView mapView = (MapView) Controller.getCurrentController().getMapViewManager().getMapViewComponent();
        direction.scroll(mapView, ResourceController.getResourceController().getIntProperty(WHEEL_VELOCITY, 80));
    }
}
