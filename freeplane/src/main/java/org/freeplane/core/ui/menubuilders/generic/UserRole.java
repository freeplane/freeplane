/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2019 dimitry
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
package org.freeplane.core.ui.menubuilders.generic;

/**
 * @author Dimitry Polivaev
 * Dec 21, 2019
 */
public enum UserRole {
	SIMPLE_VIEWER(false, false), ADVANCED_VIEWER(false, true), 
	SIMPLE_EDITOR(true, false), ADVANCED_EDITOR(true, true);

	public enum Interfaces{
		SIMPLE, ADVANCED
	}

	final boolean isEditor;
	final boolean isAdvanced;
	private UserRole(boolean isEditor, boolean isAdvanced) {
		this.isEditor = isEditor;
		this.isAdvanced = isAdvanced;
	}
	public static UserRole of(Interfaces selectedInderface, boolean canEdit) {
		switch (selectedInderface) {
			case SIMPLE:
				return canEdit ? SIMPLE_EDITOR : SIMPLE_VIEWER;
			default:
				return canEdit ? ADVANCED_EDITOR : ADVANCED_VIEWER;
		}

	}
}

