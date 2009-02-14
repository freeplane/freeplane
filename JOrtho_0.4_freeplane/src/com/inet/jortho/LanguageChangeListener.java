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
 *  Created on 06.12.2007
 */
package com.inet.jortho;

import java.util.EventListener;

/**
 * A language change event gets fired whenever the language is changed.
 * You can register a LanguageChangeListener in the class SpellChecker.
 * @author Volker Berlin
 * @see SpellChecker#addLanguageChangeLister(LanguageChangeListener)
 * @see SpellChecker#removeLanguageChangeLister(LanguageChangeListener)
 */
public interface LanguageChangeListener extends EventListener{
    
    /**
     * This method gets called when the language is changed.
     * This occurs if the user selects another language in the languages menu.
     * @param ev A LanguageChangeEvent object describing the changes.
     */
    public void languageChanged(LanguageChangeEvent ev);

}
