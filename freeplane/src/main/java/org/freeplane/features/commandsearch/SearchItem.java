package org.freeplane.features.commandsearch;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.Icon;

import org.freeplane.core.resources.ResourceController;

abstract class SearchItem implements Comparable<SearchItem>{
    
    private static final int PATTERN_CACHE_SIZE = 10;
    static final String ITEM_PATH_SEPARATOR = "->";
    private static final LinkedHashMap<String, Pattern> patterns = new LinkedHashMap<String, Pattern>(10+1) {

        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Pattern> eldest) {
            return size() > PATTERN_CACHE_SIZE;
        }
        
    };

    abstract int getItemTypeRank();
    abstract String getComparedText();
    abstract Icon getTypeIcon();
    abstract String getDisplayedText();
    abstract String getTooltip();

    /**
     *
     * @return whether to refresh list afterwards
     */
    abstract boolean execute();

    @Override
    public int compareTo(SearchItem o) {
        int rankCompare = Integer.compare(getItemTypeRank(), o.getItemTypeRank());
        return rankCompare != 0 ? rankCompare :  getComparedText().compareToIgnoreCase(o.getComparedText());
    }
    
    protected boolean checkAndMatch(final String[] searchTerms)
    {
        for (int i = 0; i < searchTerms.length; i++)
        {
            if (!checkAndMatch(searchTerms[i]))
            {
                return false;
            }
        }
        return true;
    }
    
    abstract protected boolean checkAndMatch(final String searchTerm);
    
    protected static boolean contains(String text, String word) {
        if(shouldSearchWholeWords())
            return containsWord(text, word);
        else
            return text.toLowerCase().contains(word);
    }
    static boolean shouldSearchWholeWords() {
        return ResourceController.getResourceController().getBooleanProperty("cmdsearch_whole_words");
    }
    public static boolean containsWord(String text, String word) {
    	if (word.isEmpty())
    		return false;
        return patterns.computeIfAbsent(word, SearchItem::compilePattern)
                .matcher(text).find();
    }
    
	private static Pattern compilePattern(String word) {
		boolean startsWithLetter = Character.isAlphabetic(word.codePointAt(0));
		boolean endsWithLetter = Character.isAlphabetic(word.codePointBefore(word.length()));
		String startingWordBoundary = startsWithLetter ? "\\b" : "";
		String endingWordBoundary = endsWithLetter ? "\\b" : "";
		return Pattern.compile(startingWordBoundary + Pattern.quote(word) + endingWordBoundary, Pattern.CASE_INSENSITIVE);
	}



}
