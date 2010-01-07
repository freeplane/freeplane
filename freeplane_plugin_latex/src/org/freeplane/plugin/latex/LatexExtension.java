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
package org.freeplane.plugin.latex;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.freeplane.core.extension.IExtension;

/**
 * @author Dimitry Polivaev
 * 06.12.2008
 */
class LatexExtension implements IExtension {
	private String equation;
	final private Set<JLatexViewer> viewers;

	public LatexExtension() {
		equation = "\\mbox{I}^\\fgcolor{ff0000}{\\heartsuit}\\mbox{\\JLaTeXMath}";
		viewers = new LinkedHashSet<JLatexViewer>();
	}

	public String getEquation() {
		return equation;
	}

	Set<JLatexViewer> getViewers() {
		return viewers;
	}

	void removeViewers() {
		final Iterator iterator = getViewers().iterator();
		while (iterator.hasNext()) {
			final JLatexViewer comp = (JLatexViewer) iterator.next();
			comp.getParent().remove(comp);
		}
		viewers.clear();
	}

	public void setEquation(final String equation) {
		this.equation = equation;
		final Iterator iterator = viewers.iterator();
		while (iterator.hasNext()) {
			final JLatexViewer comp = (JLatexViewer) iterator.next();
			comp.setModel(this);
		}
	}
}
