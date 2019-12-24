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
	NO_CONSTRAINT(false, false) {
		@Override
		public UserRoleConstraint and(UserRoleConstraint entryConstraint) {
			return entryConstraint;
		}
	},
	EXPERT(false, true) {
		@Override
		public UserRoleConstraint and(UserRoleConstraint entryConstraint) {
			return entryConstraint.editorRequired ? UserRoleConstraint.EXPERT_EDITOR : this;
		}
	}, 
	EDITOR(true, false) {
		@Override
		public UserRoleConstraint and(UserRoleConstraint entryConstraint) {
			return entryConstraint.expertRequired ? UserRoleConstraint.EXPERT_EDITOR : this;
		}
	}, 
	EXPERT_EDITOR(true, true) {
		@Override
		public UserRoleConstraint and(UserRoleConstraint entryConstraint) {
			return this;
		}
	};
	private final boolean editorRequired;
	private final boolean expertRequired;
	private UserRoleConstraint(boolean editorRequired, boolean expertRequired) {
		this.editorRequired = editorRequired;
		this.expertRequired = expertRequired;
	}
	
	public boolean test(UserRole role) {
		return (! editorRequired || role.isEditor) && (! expertRequired || role.isExpert);
	}


	abstract public UserRoleConstraint and(UserRoleConstraint entryConstraint);
	
}
