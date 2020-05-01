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
public enum UserRoleConstraint {
	NO_CONSTRAINT(false) {
		@Override
		public UserRoleConstraint and(UserRoleConstraint entryConstraint) {
			return entryConstraint;
		}
	},
	EDITOR(true) {
		@Override
		public UserRoleConstraint and(UserRoleConstraint entryConstraint) {
			return UserRoleConstraint.EDITOR;
		}
	}; 
	private final boolean editorRequired;
	private UserRoleConstraint(boolean editorRequired) {
		this.editorRequired = editorRequired;
	}
	
	public boolean test(UserRole user) {
		return (! editorRequired || user.isEditor);
	}


	abstract public UserRoleConstraint and(UserRoleConstraint entryConstraint);
	
}
