package docear_plugin_optionpane;

import org.docear.plugin.core.DocearPlugin;
import org.freeplane.features.mode.ModeController;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator extends DocearPlugin implements BundleActivator {

	
	
	public void stop(BundleContext context) throws Exception {
		
	}

	public void startPlugin(BundleContext context, ModeController modeController) {
//		new OptionPaneConfiguration(modeController);
	}

}
