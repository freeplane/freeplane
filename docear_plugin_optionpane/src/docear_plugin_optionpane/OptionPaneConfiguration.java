package docear_plugin_optionpane;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.docear.plugin.core.ALanguageController;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class OptionPaneConfiguration extends ALanguageController {
	private ModeController modeController;
	
	public OptionPaneConfiguration(ModeController modeController) {
		super();
		this.modeController = modeController;
		
		createOptionPane();
	}

	private void createOptionPane() {
		ModeController modeController = Controller.getCurrentModeController();
		
		modeController.addMenuContributor(new IMenuContributor() {
			public void updateMenus(ModeController modeController, MenuBuilder builder) {
				createOptionPanel();
			}
		});
		
	}
	
	private void createOptionPanel() {
		try {
			final Box panel = new Box(BoxLayout.Y_AXIS);
			System.out.println("OPTIONPANE: "+modeController);
			final JTabbedPane tabs = (JTabbedPane) modeController.getUserInputListenerFactory().getToolBar("/format").getComponent(1);
			final JScrollPane timeScrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			UITools.setScrollbarIncrement(timeScrollPane);
			tabs.add(TextUtils.getText("options_panel"), timeScrollPane);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
