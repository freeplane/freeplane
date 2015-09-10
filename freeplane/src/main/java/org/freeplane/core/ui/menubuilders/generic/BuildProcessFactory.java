package org.freeplane.core.ui.menubuilders.generic;

public interface BuildProcessFactory {

	public PhaseProcessor getBuildProcessor();

	public SubtreeProcessor getChildProcessor();

}
