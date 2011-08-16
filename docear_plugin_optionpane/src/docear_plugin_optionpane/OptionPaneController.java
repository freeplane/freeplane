package docear_plugin_optionpane;

import javax.swing.JScrollPane;

public class OptionPaneController {
	private static OptionPaneController optionPanelController;	
	
	private JScrollPane optionsPane;
	
	

	public OptionPaneController() {
		optionPanelController = this;
	}
	
	public static OptionPaneController getController() {
		return optionPanelController;
	}
	
	public JScrollPane getOptionspane() {
		return optionsPane;
	}

	public void setOptionspane(JScrollPane optionspane) {
		this.optionsPane = optionspane;
	}
	
	
}
