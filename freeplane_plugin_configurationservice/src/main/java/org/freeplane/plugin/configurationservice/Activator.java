package org.freeplane.plugin.configurationservice;

import java.net.URL;
import java.util.Hashtable;

import javax.swing.Icon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.ConditionalContentTransformer;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	private static final String CONFIGURATIONSERVICE_URL = "configurationservice.url";
	private static final String FORMULA_DISABLE_CACHING = "formula_disable_caching";
	private static final String MENU_BAR_PARENT_LOCATION = "/menu_bar/extras/first";
	static final String MENU_BAR_LOCATION = MENU_BAR_PARENT_LOCATION + "/formula";
	
	private static final String TOGGLE_PARSE_FORMULAS = "parse_formulas";

	private final class ConfigurationServicePluginRegistration implements IModeControllerExtensionProvider {
		private static final String PREFERENCES_RESOURCE = "preferences.xml";

		public void installExtension(ModeController modeController) {
			
			int port =  Integer.parseInt(System.getProperty("freeplane.configurationservice.port", "0"));
			
			addPluginDefaults();
			addPreferencesToOptionPanel();
			
			ConfigurationSession configurationSession = new ConfigurationSession();
			final StartConfigurationSessionAction startAction = new StartConfigurationSessionAction(configurationSession);
			final UpdateConfigurationAction updateAction = new UpdateConfigurationAction(configurationSession);
			modeController.addAction(startAction);
			modeController.addAction(updateAction);
			
			TCPServer srv = new TCPServer(port, configurationSession);
	        Thread t = new Thread(srv);
	        t.start();
		}

		private void addPreferencesToOptionPanel() {
			final URL preferences = this.getClass().getResource(PREFERENCES_RESOURCE);
			if (preferences == null)
				throw new RuntimeException("cannot open preferences");
			final Controller controller = Controller.getCurrentController();
			MModeController modeController = (MModeController) controller.getModeController();
			modeController.getOptionPanelBuilder().load(preferences);
		}

		private void addPluginDefaults() {
			final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
			if (defaults == null)
				throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
			Controller.getCurrentController().getResourceController().addDefaults(defaults);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		props.put("mode", new String[] { MModeController.MODENAME /*TODO: browse mode too?*/});
		context.registerService(IModeControllerExtensionProvider.class.getName(), new ConfigurationServicePluginRegistration(),
		    props);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
	}
}
