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
 *  Created on 12.12.2007
 */
package com.inet.jortho;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A hash list of Suggestions. The list is cut with a max dissimilarity. If a suggestion already exist then the
 * suggestion with the lower dissimilarity will be hold.
 * 
 * @author Volker Berlin
 */
class Suggestions {

    private final int maxDiff;
    private final HashMap<Suggestion,Suggestion> map = new HashMap<Suggestion,Suggestion>();

    /**
     * Create a suggestions list. Suggestion with a larger dissimilarity can not be added.
     * @param maxDiff the max dissimilarity
     */
    Suggestions(int maxDiff){
        this.maxDiff = maxDiff;
    }
    
    /**
     * Add a suggestion. 
     * @param suggestion the suggestion 
     */
    void add(Suggestion suggestion){
        if(suggestion.getDissimilarity() > maxDiff){
            return;
        }
        Suggestion oldSuggestion = map.get( suggestion );
        if(oldSuggestion != null && oldSuggestion.getDissimilarity() <= suggestion.getDissimilarity()){
            return;
        }
        map.put( suggestion, suggestion );
    }
    
    
    List<Suggestion> getlist(){
        ArrayList<Suggestion> list = new ArrayList<Suggestion>();
        for(Suggestion sugg : map.values()){
            list.add( sugg );
        }
        return list;
    }

    /**
     * Get the max dissimilarity. Suggestion with a larger value can not be added.
     * @return
     */
    int getMaxDissimilarity() {
        return maxDiff;
    }
}
