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
 *  Created on 02.11.2005
 */
package com.inet.jortho;

import java.io.*;
import java.util.zip.*;

/**
 * A container for a word list.
 * @author Volker Berlin
 */
final class Dictionary extends DictionaryBase{

   
    /**
     * Create an empty Dictionary.
     */
    public Dictionary(){
        tree = new char[10000];
        tree[size++] = LAST_CHAR;
    }
    
    
    /**
     * Create an Dictionary from a serialize Dictionary. This is used from the DictionaryFactory.
     * @see #toArray()
     * @see DictionaryFactory
     */
    public Dictionary(char[] tree){
        super(tree);
    }
    
    
    /**
     * Save this dictionary to a compressed file.
     * @param filename the name of the file.
     * @return the size in bytes that was needed.
     * @throws IOException if an I/O error occurs.
     */
    public long save(String filename) throws IOException{
        // Daten komprimieren und speichern
        File file = new File(filename);
        FileOutputStream fos = new FileOutputStream(file);
        save(fos);
        return file.length();
    }
    
    
    /**
     * Save this dictionary to the OutputStream. The data will be compressed. After finish the OutputStream is closed.
     * @param stream the OutputStream
     * @throws IOException if an I/O error occurs.
     */
    public void save(OutputStream stream) throws IOException{
        Deflater deflater = new Deflater();
        deflater.setLevel(Deflater.BEST_COMPRESSION);
        DeflaterOutputStream zip = new DeflaterOutputStream(stream, deflater);
        for(int i=0; i<size; i++){
            zip.write(tree[i]);
            zip.write(tree[i] >> 8);
        }
        
        zip.flush();
        zip.close();
    }
    
    
    /**
     * Load the directory from a compressed file.
     * @param filename the name of the file.
     * @throws IOException if an I/O error occurs.
     */
    public void load(String filename) throws IOException{
        FileInputStream fos = new FileInputStream(filename);
        load(fos);
    }

    
    /**
     * Load the directory from a compressed stream.
     * @param stream the InputStream
     * @throws IOException if an I/O error occurs.
     */
    public void load(InputStream stream)  throws IOException{
        InputStream zip = new InflaterInputStream(stream);
        zip = new BufferedInputStream(zip);
        size = 0;
        while(zip.available() > 0){
            char c = (char)(zip.read() + (zip.read() << 8));
            checkSize(size+1);
            tree[size++] = c;
        }
        zip.close();
        // Shrinken
        trimToSize();
    }
    
    
    /**
     * Trims the capacity of this <tt>Dictionary</tt> instance to be the
     * current size.  An application can use this operation to minimize
     * the storage of an <tt>Dictionary</tt> instance.
     * The load methods already call it.
     */
    void trimToSize(){
        char[] temp = new char[size];
        System.arraycopy( tree, 0, temp, 0, size );
        tree = temp;
    }


    /**
     * Add a word to the tree. If it already exist then it has no effect. 
     * @param word the new word.
     */
    public void add(String word){
        idx = 0;
        for(int i=0; i<word.length(); i++){
            char c = word.charAt(i);
            searchCharOrAdd( c );
            if(i == word.length()-1){
                tree[idx+1] |= 0x8000;
                return;
            }
            int nextIdx = readIndex();
            if(nextIdx == 0){
                idx = createNewNode();
            }else{
                idx = nextIdx;
            }
        }
    }
    
    
    /**
     * Convert the directory tree to char array.
     * @return a char array that include the data of the dictionary.
     */
    public char[] toArray(){
        char[] puffer = new char[size];
        System.arraycopy(tree, 0, puffer, 0, size);
        return puffer; 
    }
    
    
    /**
     * Get the size of chars that this dictionary need in memory.
     */
    public int getDataSize(){
        return size;
    }
    
    
    private void searchCharOrAdd(char c){
        if(c == LAST_CHAR)
            throw new RuntimeException("Invalid Character");
        while(idx<size && tree[idx] < c){
            idx += 3;
        }
        if(idx>=size)
            throw new RuntimeException("Internal Error");
        if(tree[idx] == c){
            return;
        }
        insertChar(c);
        return;
    }


    private void insertChar(char c) {
        checkSize(size+3);
        System.arraycopy(tree, idx, tree, idx+3, size-idx);
        tree[idx] = c;
        tree[idx+1] = 0;
        tree[idx+2] = 0;
        size += 3;
        for(int i=0; i<size; ){
            if(tree[i] == LAST_CHAR){
                i++;
            }else{
                int index = (tree[i+1]<<16) + tree[i+2];
                int indexValue = index & 0x7fffffff;
                if(indexValue > idx){
                    index += 3;
                    tree[i+1] = (char)(index >> 16);
                    tree[i+2] = (char)(index);
                }
                i += 3;
            }
        }
    }
    
    
    /**
     * Create a new node at end of the array.
     * On the current idx position is writing the pointer.
     * The pointer on the current idx position must be 0 without some word end flags (0x8000 on idx+1)
     * @return Pointer on new node.
     */
    private final int createNewNode() {
        checkSize(size+1);
        tree[idx+1] |= (char)(size >> 16);
        tree[idx+2] |= (char)(size);
        idx = size;
        tree[idx  ] = LAST_CHAR;
        size += 1;
        return idx;
    }


    /**
     * Check the size of the array and resize it if needed.
     * @param newSize the requied size
     */
    private final void checkSize(int newSize){
        if(newSize > tree.length){
            char[] puffer = new char[Math.max(newSize, 2*tree.length)];
            System.arraycopy(tree, 0, puffer, 0, size);
            tree = puffer;
        }
    }
}
