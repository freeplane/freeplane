/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is to be deleted.
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
package deprecated.freemind.modes.mindmapmode.actions.undo;

import java.awt.event.ActionEvent;

import org.freeplane.controller.FreeplaneAction;
import org.freeplane.modes.mindmapmode.MModeController;

import deprecated.freemind.modes.mindmapmode.actions.instance.ActionInstance;
import deprecated.freemind.modes.mindmapmode.actions.instance.CompoundActionInstance;

/**
 * @author foltin
 */
public class CompoundActionHandler extends FreeplaneAction implements IActor {
	public CompoundActionHandler(final MModeController c) {
		super();
		c.getActionFactory().registerActor(this, getDoActionClass());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.controller.actions.ActorXml#act(freemind.controller.actions.
	 * generated.instance.XmlAction)
	 */
	public void act(final ActionInstance action) {
		final CompoundActionInstance compound = (CompoundActionInstance) action;
		final Object[] actions = compound.getListChoiceList().toArray();
		for (int i = 0; i < actions.length; i++) {
			final Object obj = actions[i];
			if (obj instanceof ActionInstance) {
				final ActionInstance xmlAction = (ActionInstance) obj;
				final IActor actor = getMModeController().getActionFactory()
				    .getActor(xmlAction);
				actor.act(xmlAction);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.controller.actions.ActorXml#getDoActionClass()
	 */
	public Class getDoActionClass() {
		return CompoundActionInstance.class;
	}
}
