/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 dimitry
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
package com.inet.jortho;

import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;


/**
 * @author Dimitry Polivaev
 * Aug 14, 2010
 */
class LetterBasedBreakIterator {

	private CharacterIterator text;

	public LetterBasedBreakIterator() {
    }

     /**
     * Set a new text string to be scanned.  The current scan
     * position is reset to first().
     * @param newText new text to scan.
     */
    public void setText(String newText)
    {
        setText(new StringCharacterIterator(newText));
    }

    /**
     * Set a new text for scanning.  The current scan
     * position is reset to first().
     * @param newText new text to scan.
     */
    public void setText(CharacterIterator text){
    	this.text = text;
    }
    
    /**
     * Returns the first boundary. The iterator's current position is set
     * to the first text boundary.
     * @return The character index of the first text boundary.
     */
	public int first() {
		return nextBoundary(text.first(), false);
	}

	private int nextBoundary(char c, boolean wasLetter) {
		for(;;){
			if(c == CharacterIterator.DONE){
				return BreakIterator.DONE;
			}
			if(wasLetter != isLetter(c)){
				return text.getIndex();
			}
			c = text.next();
		}
	}

	/**
     * Returns the first boundary following the specified character offset. If the
     * specified offset equals to the last text boundary, it returns
     * <code>BreakIterator.DONE</code> and the iterator's current position is unchanged.
     * Otherwise, the iterator's current position is set to the returned boundary.
     * The value returned is always greater than the offset or the value
     * <code>BreakIterator.DONE</code>.
     * @param offset the character offset to begin scanning.
     * @return The first boundary after the specified offset or
     * <code>BreakIterator.DONE</code> if the last text boundary is passed in
     * as the offset.
     * @exception  IllegalArgumentException if the specified offset is less than
     * the first text boundary or greater than the last text boundary.
     */
	public int following(int wordOffset) {
		if(wordOffset <= text.getBeginIndex()){
			return first();
		}
		if(wordOffset >= text.getEndIndex()){
			return BreakIterator.DONE;
		}
		int lastIndex = text.getIndex();
		text.setIndex(wordOffset);
		boolean wasLetter = isLetter(text.current());
		int nextBoundary = nextBoundary(text.next(), wasLetter);
		if(nextBoundary == BreakIterator.DONE){
			text.setIndex(lastIndex);
		}
		return nextBoundary;
    }

    /**
     * Returns the boundary following the current boundary. If the current boundary
     * is the last text boundary, it returns <code>BreakIterator.DONE</code> and
     * the iterator's current position is unchanged. Otherwise, the iterator's
     * current position is set to the boundary following the current boundary.
     * @return The character index of the next text boundary or
     * <code>BreakIterator.DONE</code> if the current boundary is the last text
     * boundary.
     * Equivalent to next(1).
     * @see #next(int)
     */
	public int next() {
		boolean wasLetter = isLetter(text.current());
		return nextBoundary(text.next(), wasLetter);
    }

	private boolean isLetter(char current) {
	    return Character.isDigit(current) || Character.isLetter(current);
    }

}
