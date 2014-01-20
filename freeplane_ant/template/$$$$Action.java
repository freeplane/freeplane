import java.awt.event.ActionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;

public class $$$$Action extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	public $$$$Action() {
		super("$$$$","$$$$", null);
	}

    @Override
    public void actionPerformed(final ActionEvent e) {
		/*TODO: enter your GUI code here*/
		UITools.informationMessage("Hi!\n\tThis is plugin $$$$");
	}
}
