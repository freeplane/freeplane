/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.main.application;

import java.util.Set;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 * 24.12.2012
 */
public interface FreeplaneStarter {
	public void setDontLoadLastMaps();

	public Controller createController();

	public void createModeControllers(final Controller controller);

	public void buildMenus(final Controller controller, final Set<String> plugins);

	public void createFrame(final String[] args);

	public void loadMapsLater(final String[] args);

	public void stop();

	public ResourceController getResourceController();
}
