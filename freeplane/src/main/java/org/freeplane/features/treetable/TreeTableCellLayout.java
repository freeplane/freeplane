package org.freeplane.features.treetable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.core.util.HtmlUtils;

public class TreeTableCellLayout {
	private static final Pattern TAG = Pattern.compile("<\\s*/?\\s*([a-zA-Z0-9]+)");
	private static final Set<String> UNFORMATTED_TAGS = new HashSet<String>(
		Arrays.asList("html", "head", "body", "p", "br"));

	public static int wrapWidth(final int columnNodeWidth, final int textPadding) {
		return Math.max(1, columnNodeWidth - textPadding);
	}

	public static int requiredRowHeight(final int[] textHeights, final int textPadding) {
		int highest = 0;
		for (final int textHeight : textHeights)
			highest = Math.max(highest, textHeight);
		return highest == 0 ? 0 : highest + 2 * textPadding;
	}

	public static String simplifiedValue(final String value) {
		if (value == null || !HtmlUtils.isHtml(value) || hasFormatting(value))
			return value;
		return HtmlUtils.htmlToPlain(value).trim();
	}

	private static boolean hasFormatting(final String html) {
		final Matcher tag = TAG.matcher(html);
		while (tag.find()) {
			if (!UNFORMATTED_TAGS.contains(tag.group(1).toLowerCase()))
				return true;
		}
		return false;
	}

	private TreeTableCellLayout() {
	}
}
