/*
 *  JOrtho
 *
 *  Copyright (C) 2005-2008 by i-net software
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 *  
 *  Created on 07.11.2005
 */
package com.inet.jortho;

import javax.swing.JPopupMenu;

/**
 * @author Volker Berlin
 */
class CheckerPopup extends JPopupMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	CheckerPopup(final SpellCheckerOptions options) {
		final CheckerListener listener = new CheckerListener(this, options);
		super.addPopupMenuListener(listener);
	}
}
