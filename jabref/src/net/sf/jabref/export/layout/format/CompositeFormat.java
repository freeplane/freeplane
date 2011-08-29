package net.sf.jabref.export.layout.format;

import net.sf.jabref.export.layout.LayoutFormatter;

/**
 * A layout formatter that is the composite of the given Formatters executed in
 * order.
 * 
 * @author $Author: coezbek $
 * @version $Revision: 1748 $ ($Date: 2006-09-03 17:20:38 +0200 (Sun, 03 Sep 2006) $)
 * 
 */
public class CompositeFormat implements LayoutFormatter {

	LayoutFormatter[] formatters;

	/**
	 * If called with this constructor, this formatter does nothing.
	 */
	public CompositeFormat() {
		// Nothing
	}

	public CompositeFormat(LayoutFormatter first, LayoutFormatter second) {
		formatters = new LayoutFormatter[] { first, second };
	}

	public CompositeFormat(LayoutFormatter[] formatters) {
		this.formatters = formatters;
	}

	public String format(String fieldText) {
		if (formatters != null) {
			for (int i = 0; i < formatters.length; i++) {
				fieldText = formatters[i].format(fieldText);
			}
		}
		return fieldText;
	}

}
