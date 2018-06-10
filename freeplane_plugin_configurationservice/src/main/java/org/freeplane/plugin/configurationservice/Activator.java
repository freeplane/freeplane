package org.freeplane.plugin.configurationservice;

import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.main.application.ApplicationLifecycleListener;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	private static final String MENU_BAR_PARENT_LOCATION = "/menu_bar/extras/first";
	static final String MENU_BAR_LOCATION = MENU_BAR_PARENT_LOCATION + "/formula";

	private final class ConfigurationServicePluginRegistration implements IModeControllerExtensionProvider{
		private static final String PREFERENCES_RESOURCE = "preferences.xml";

		@Override
		public void installExtension(ModeController modeController) {

			int port =  Integer.parseInt(System.getProperty("freeplane.configurationservice.port", "0"));

			addPluginDefaults();
			addPreferencesToOptionPanel();

			ConfigurationSession configurationSession = new ConfigurationSession();
			final StartConfigurationSessionAction startAction = new StartConfigurationSessionAction(configurationSession);
			final UpdateConfigurationAction updateAction = new UpdateConfigurationAction(configurationSession);
			modeController.addAction(startAction);
			modeController.addAction(updateAction);

			modeController.getController().addApplicationLifecycleListener(
				new ConfigurationServiceRunner());

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

	private final class ConfigurationServiceRunner implements ApplicationLifecycleListener{

		@Override
		public void onStartupFinished() {
			String mindMapFile = System.getProperty("freeplane.configurationservice.mindMapFile");
			System.out.println("MM: " + mindMapFile);
			ConfigurationSession configurationSession = new ConfigurationSession();
			configurationSession.start(mindMapFile);
			runUpdate(configurationSession, 22);
			runUpdate(configurationSession, 33);

		}

		@Override
		public void onApplicationStopped() {
		}

		private void runUpdate(ConfigurationSession configurationSession, int value) {
			configurationSession.update("ID_1053277958", "a", value);

			List<String> attributesList = new ArrayList<>();
			attributesList.add("a");
			attributesList.add("b");
			attributesList.add("area");

			Map<String, Object> attributeMap = configurationSession.readValues("ID_1053277958", attributesList);

			for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
				LogUtils.info(entry.getKey() + " " + entry.getValue());
			}
		}
	}


	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		props.put("mode", new String[] { MModeController.MODENAME});
		context.registerService(IModeControllerExtensionProvider.class.getName(),
			new ConfigurationServicePluginRegistration(), props);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
	}
}
