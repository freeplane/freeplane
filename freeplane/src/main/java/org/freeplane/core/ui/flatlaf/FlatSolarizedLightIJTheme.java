package org.freeplane.core.ui.flatlaf;

import com.formdev.flatlaf.IntelliJTheme;

public class FlatSolarizedLightIJTheme extends IntelliJTheme.ThemeLaf
{
	private static final long serialVersionUID = 1L;
	public static final String NAME = "Solarized Light";

	public FlatSolarizedLightIJTheme() {
		super( Utils.loadTheme( "SolarizedLight.theme.json" ) );
	}

	@Override
	public String getName() {
		return NAME;
	}
}

