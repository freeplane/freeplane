package org.freeplane.core.ui.menubuilders.generic;

import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase;

public interface BuildPhaseListener {

	void buildPhaseFinished(Phase actions, Entry entry);
}
