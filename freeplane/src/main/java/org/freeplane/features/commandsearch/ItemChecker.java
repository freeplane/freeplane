package org.freeplane.features.commandsearch;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

class ItemChecker {
	private static final int PATTERN_CACHE_SIZE = 10;
	private static final LinkedHashMap<String, Pattern> patterns = new LinkedHashMap<String, Pattern>(10+1) {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<String, Pattern> eldest) {
			return size() > PATTERN_CACHE_SIZE;
		}

	};
	private final boolean shouldSearchWholeWords;

	public ItemChecker(boolean shouldSearchWholeWords) {
		super();
		this.shouldSearchWholeWords = shouldSearchWholeWords;
	}

    void findMatchingItems(List<? extends SearchItem> items, final String[] searchTerms,
            final Consumer<SearchItem> matches) {
        for (final SearchItem prefsItem: items)
        {
            if (prefsItem.checkAndMatch(searchTerms, this))
            {
                matches.accept(prefsItem);
            }
        }
    }

	boolean contains(String text, String word) {
		if(shouldSearchWholeWords)
			return containsWord(text, word);
		else
			return text.toLowerCase().contains(word);
	}
    
	private boolean containsWord(String text, String word) {
		if (word.isEmpty())
			return false;
		return patterns.computeIfAbsent(word, ItemChecker::compilePattern)
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