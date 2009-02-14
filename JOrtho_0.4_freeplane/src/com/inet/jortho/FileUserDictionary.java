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
 *  Created on 23.12.2007
 */
package com.inet.jortho;

import java.io.*;
import java.util.Locale;



/**
 * This is a reference implementation of the interface {@link UserDictionaryProvider}.
 * It save the user dictionaries on the local disk as text files.
 * @author Volker Berlin
 */
public class FileUserDictionary implements UserDictionaryProvider{

    private final String fileBase;
    private File file;
    
    /** 
     * Create a FileUserDictionary with the dictionaries in the root of the current
     * application.
     */
    public FileUserDictionary(){
        this( "" );
    }
    
    /**
     * Create a FileUserDictionary with the dictionaries on a specific location.
     * @param fileBase the base 
     */
    public FileUserDictionary( String fileBase ){
        if( fileBase == null ){
            fileBase = "";
        }
        fileBase = fileBase.trim();
        fileBase = fileBase.replace( '\\', '/' );
        if( fileBase.length() > 0 && !fileBase.endsWith("/") ){
            fileBase += "/";
        }
        this.fileBase = fileBase;
    }
    
    /**
     * {@inheritDoc}
     */
    public void addWord(String word){
        try{
            FileOutputStream output = new FileOutputStream( file, true );
            Writer writer = new OutputStreamWriter( output, "UTF8" );
            if( file.length() > 0 ){
                writer.write( "\n" );
            }
            writer.write( word );
            writer.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }


    /**
     * {@inheritDoc}
     */
    public String getUserWords(Locale locale){
        file = new File(fileBase + "UserDictionary_" + locale + ".txt" );
        try{
            FileInputStream input = new FileInputStream(file);
            Reader reader = new InputStreamReader( input, "UTF8" );
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[4096];
            int count;
            while( (count = reader.read(buffer)) > 0 ){
                builder.append( buffer, 0, count );
            }
            reader.close();
            return builder.toString();
        }catch(IOException ex){
            /* ignore FileNotFound */
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setUserWords(String wordList){
        try{
            FileOutputStream output = new FileOutputStream( file );
            Writer writer = new OutputStreamWriter( output, "UTF8" );
            writer.write( wordList );
            writer.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
