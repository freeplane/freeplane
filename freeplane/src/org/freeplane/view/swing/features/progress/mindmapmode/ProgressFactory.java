/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.view.swing.features.progress.mindmapmode;

import org.freeplane.features.mode.ModeController;

/**
 * @author Dimitry Polivaev
 * Jun 13, 2011
 */
public class ProgressFactory {
    public void installActions(ModeController modeController){
        modeController.addAction(new ProgressUpAction());
        modeController.addAction(new ProgressDownAction());
        modeController.addAction(new ExtendedProgress10Action());
        modeController.addAction(new ExtendedProgress25Action());
        modeController.addAction(new RemoveProgressAction());
    }
}
