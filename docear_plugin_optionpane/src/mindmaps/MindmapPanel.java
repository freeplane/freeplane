package mindmaps;

import javax.swing.JTextPane;

import docear_plugin_optionpane.OptionPaneController;

public class MindmapPanel {
	
	public MindmapPanel() {
		OptionPaneController optionPaneController = OptionPaneController.getController();
		optionPaneController.getOptionspane().add(new JTextPane(), 0);
	}
}
