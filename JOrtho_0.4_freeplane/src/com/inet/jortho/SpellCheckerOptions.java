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
 * Created on 20.02.2008
 */
package com.inet.jortho;

/**
 * This class contains some options for spell checking. You can change it globally(see ({@link SpellChecker#getOptions()}) or
 * for every JTextComponent individually on registering.
 *
 * @author Volker Berlin
 */
public class SpellCheckerOptions {

    private int     suggestionsLimitMenu   = 15;

    private int     suggestionsLimitDialog = 15;

    private boolean caseSensitive          = true;

    private boolean ignoreCapitalization   = false;

    private boolean ignoreAllCaps          = true;

    private boolean ignoreNumbers          = false;

    /**
     * Create SpellCheckerOptions with default values.
     */
    public SpellCheckerOptions() {
        //empty block
    }

    /**
     * Set the maximun count of entries for the suggestion menu. The default is 15.
     * 
     * @param count
     *            the suggestion limit for the suggestion menu
     */
    public void setSuggestionsLimitMenu( int count ) {
        this.suggestionsLimitMenu = count;
    }

    /**
     * Get the maximum count of entries for the suggestion menu.
     *
     * @return the suggestion limit
     */
    public int getSuggestionsLimitMenu() {
        return suggestionsLimitMenu;
    }

    /**
     * Set the maximun count of entries for the suggestion list in the spell checker dialog. The default is 15.
     * 
     * @param count
     *            the suggestion limit for the spell checker dialog
     */
    public void setSuggestionsLimitDialog( int count ) {
        this.suggestionsLimitDialog = count;
    }

    /**
     * Get the maximun count of enties for the suggestion list in the spell checker dialog.
     * 
     * @return the suggestions limit
     */
    public int getSuggestionsLimitDialog() {
        return suggestionsLimitDialog;
    }

    /**
     * Set whether the spell checker is case-sensitive. This only has an effect on the first letter of a word. The default
     * value is true.
     *
     * @param caseSensitive whether the spell checker is to be case-sensitive for the first letter of each word.
     */
    public void setCaseSensitive( boolean caseSensitive ) {
        this.caseSensitive = caseSensitive;
    }

    /**
     * Return whether the spell checker is case-sensitive. This only has an effect on the first letter of a word.
     *
     * @return whether the spell checker is case-sensitive.
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * Set whether capitalized words should be correct if the word is in the dictionary as lower-case. This is often used in a title or in
     * names. The first word of a sentence will always ignore the capitalization. The default is false.
     *
     * @param ignore
     *            the new value
     */
    public void setIgnoreCapitalization( boolean ignore ) {
        ignoreCapitalization = ignore;
    }

    /**
     * Return whether capitalized words should be correct if the word is in the dictionary as lower-case.
     *
     * @return true, if capitalization is to be ignored
     */
    public boolean getIgnoreCapitalization() {
        return ignoreCapitalization;
    }

    /**
     * Set if words that are entirely in uppercase should be ignored for spell checking. This is most often used for
     * abbreviations such DNS or HTTP. The default value is true.
     *
     * @param ignore
     *            the new value
     */
    public void setIgnoreAllCapsWords( boolean ignore ) {
        ignoreAllCaps = ignore;
    }

    /**
     * Whether words entirely in upper-case should be ignored for spell checking.
     * 
     * @return true, if all entirely upper-case words are to be ignored
     */
    public boolean isIgnoreAllCapsWords() {
        return ignoreAllCaps;
    }

    /**
     * Set if words that include a number in any position should be ignored for spell checking. The default is false.
     *
     * @param ignore
     *            the new value
     */
    public void setIgnoreWordsWithNumbers( boolean ignore ) {
        ignoreNumbers = ignore;
    }

    /**
     * Whether words that include a number in any position should be ignored for spell checking.
     *
     * @return true, if words with numbers in any position are to be ignored
     */
    public boolean isIgnoreWordsWithNumbers() {
        return ignoreNumbers;
    }
}
