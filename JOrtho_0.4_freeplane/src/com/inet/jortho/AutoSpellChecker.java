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
 *  Created on 05.11.2005
 */
package com.inet.jortho;

import java.util.Locale;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.Highlighter.Highlight;

/**
 * This class check a <code>JTextComponent</code> automatically (in the background) for orthography. Spell error are
 * highlighted with a red zigzag line.
 * 
 * @author Volker Berlin
 */
class AutoSpellChecker implements DocumentListener, LanguageChangeListener {
    private static final RedZigZagPainter painter = new RedZigZagPainter();

    private final JTextComponent                jText;
    private final SpellCheckerOptions options;

    private Dictionary                    dictionary;

    private Locale                        locale;

    
    public AutoSpellChecker(JTextComponent text, SpellCheckerOptions options){
        this.jText = text;
        this.options = options == null ? SpellChecker.getOptions() : options;
        jText.getDocument().addDocumentListener( this );

        SpellChecker.addLanguageChangeLister( this );
        dictionary = SpellChecker.getCurrentDictionary();
        locale = SpellChecker.getCurrentLocale();
        checkAll();
    }

    /**
     * Remove the AutoSpellChecker from the given JTextComponent.
     * 
     * @param text
     *            the JTextComponent
     */
    static void disable( JTextComponent text ){
        AbstractDocument doc = (AbstractDocument)text.getDocument();
        for(DocumentListener listener : doc.getDocumentListeners()){
            if(listener instanceof AutoSpellChecker){
                AutoSpellChecker autoSpell = (AutoSpellChecker)listener;
                doc.removeDocumentListener( autoSpell );
                removeHighlights(text);
            }
        }
    }

	private static void removeHighlights(JTextComponent text) {
	    Highlighter highlighter = text.getHighlighter();
	    for( Highlight highlight : highlighter.getHighlights() ) {
	        if( highlight.getPainter() == painter ){
	            highlighter.removeHighlight( highlight );
	        }
	    }
    }
    
    /**
     * Refresh the highlighting. This can be useful if the dictionary was modify.
     * 
     * @param text
     *            the JTextComponent
     */
    static void refresh( JTextComponent text ){
        AbstractDocument doc = (AbstractDocument)text.getDocument();
        for(DocumentListener listener : doc.getDocumentListeners()){
            if( listener instanceof AutoSpellChecker ){
                AutoSpellChecker autoSpell = (AutoSpellChecker)listener;
                autoSpell.checkAll();
            }
        }
    }

    /*====================================================================
     * 
     * Methods of interface DocumentListener
     * 
     *===================================================================*/

    /**
     * {@inheritDoc}
     */
    public void changedUpdate( DocumentEvent ev ) {
        //Nothing
    }

    /**
     * {@inheritDoc}
     */
    public void insertUpdate( DocumentEvent ev ) {
        checkElements( ev.getOffset(), ev.getLength() );
    }

    /**
     * {@inheritDoc}
     */
    public void removeUpdate( DocumentEvent ev ) {
        checkElements( ev.getOffset(), 0 );
    }

    /**
     * Check the Elements on the given position.
     */
    private void checkElements( int offset, int length ) {
        int end = offset + length;
        Document document = jText.getDocument();
        Element element;

        do{
            try {
                // We need to use a ParagraphElement because a CharacterElement produce problems with formating in a word
                element = ((AbstractDocument)document).getParagraphElement( offset );
            } catch( java.lang.Exception ex ) {
                return;
            }
            checkElement( element );
            offset = element.getEndOffset();
        }while( offset <= end && offset < document.getLength() );
    }

    /**
     * Check the spelling of the text of an element.
     * 
     * @param element
     *            the to checking Element
     */
    private void checkElement( javax.swing.text.Element element ) {
        try {
            int i = element.getStartOffset();
            int j = element.getEndOffset();
            Highlighter highlighter = jText.getHighlighter();
            Highlight[] highlights = highlighter.getHighlights();
            for( int k = highlights.length; --k >= 0; ) {
                Highlight highlight = highlights[k];
                int hlStartOffset = highlight.getStartOffset();
                int hlEndOffset = highlight.getEndOffset();
                if( (i <= hlStartOffset && hlStartOffset <= j) || 
                    (i <= hlEndOffset && hlEndOffset <= j) ) {
                    highlighter.removeHighlight( highlight );
                }
            }

            int l = ((AbstractDocument)jText.getDocument()).getLength();
            j = Math.min( j, l );
            if( i >= j )
                return;

            // prevent a NPE if the dictionary is currently not loaded.
            Dictionary dic = dictionary;
            Locale loc = locale;
            if( dic == null || loc == null ){
                return;
            }
            
            Tokenizer tok = new Tokenizer( jText, dic, loc, i, j, options );
            String word;
            while( (word = tok.nextInvalidWord()) != null ) {
                int wordOffset = tok.getWordOffset();
                highlighter.addHighlight( wordOffset, wordOffset + word.length(), painter );
            }
        } catch( BadLocationException e ) {
            e.printStackTrace();
        }
    }

    /**
//     * Check the completely text. Because this can consume many times with large Documents that this will do in a thread
     * in the background step by step.
     */
    private void checkAll() {
        if( jText == null ) {
            //the needed objects does not exists
            return;
        }
        if( dictionary == null) {
            removeHighlights(jText);
            return;
        }

        Thread thread = new Thread( new Runnable() {
            public void run() {
                Document document = jText.getDocument();
                for( int i = 0; i < document.getLength(); ) {
                    try {
                        final Element element = ((AbstractDocument)document).getParagraphElement( i );
                        i = element.getEndOffset();
                        SwingUtilities.invokeLater( new Runnable() {
                            public void run() {
                                checkElement( element );
                            }

                        } );
                    } catch( java.lang.Exception ex ) {
                        return;
                    }
                }
            }
        }, "JOrtho checkall" );
        thread.setPriority( Thread.NORM_PRIORITY - 1 );
        thread.setDaemon( true );
        thread.start();
    }

    /**
     * {@inheritDoc}
     */
    public void languageChanged( LanguageChangeEvent ev ) {
        dictionary = SpellChecker.getCurrentDictionary();
        locale = SpellChecker.getCurrentLocale();
        checkAll();
    }

}
