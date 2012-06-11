package org.freeplane.features.text.mindmapmode;

import org.freeplane.core.resources.IFreeplanePropertyListener;

import com.lightdev.app.shtm.SHTMLPrefsChangeListener;

/**
 * This class forwards changes in Freeplane pref properties that are relevant to simplyhtml
 * ("simplyhtml.*") to a SHTMLPrefsChangeListener.
 * @author Felix Natter
 *
 */
public class FreeplaneToSHTMLPropertyChangeAdapter implements IFreeplanePropertyListener
{
	private final SHTMLPrefsChangeListener shtmlPrefsChangedListener;
	public FreeplaneToSHTMLPropertyChangeAdapter(final SHTMLPrefsChangeListener shtmlPrefsChangeListener)
	{
		this.shtmlPrefsChangedListener = shtmlPrefsChangeListener;
	}
	
	public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
		if (propertyName.startsWith("simplyhtml."))
		{
			final String shtmlProp = propertyName.substring("simplyhtml.".length());
			shtmlPrefsChangedListener.shtmlPrefChanged(shtmlProp, newValue, oldValue);
		}
	}
	
}
