package org.freeplane.plugin.formula;

import java.net.URL;
import java.util.Hashtable;

import javax.swing.JMenu;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	private static final String FORMULA_DISABLE_PLUGIN = "formula_disable_plugin";
	private static final String FORMULA_DISABLE_CACHING = "formula_disable_caching";
	private static final String MENU_BAR_PARENT_LOCATION = "/menu_bar/extras/first";
	static final String MENU_BAR_LOCATION = MENU_BAR_PARENT_LOCATION + "/formula";

	private final class FormulaPluginRegistration implements IModeControllerExtensionProvider {
		private static final String PREFERENCES_RESOURCE = "preferences.xml";

		public void installExtension(ModeController modeController) {
			addPluginDefaults();
			addPreferencesToOptionPanel();
			final boolean disablePluginProperty = ResourceController.getResourceController().getBooleanProperty(
			    FORMULA_DISABLE_PLUGIN);
			if (!disablePluginProperty) {
				addMenuItems(modeController);
				TextController.getController(modeController).addTextTransformer(new FormulaTextTransformer());
				final FormulaUpdateChangeListener listener = new FormulaUpdateChangeListener();
				modeController.getMapController().addNodeChangeListener(listener);
				modeController.getMapController().addMapChangeListener(listener);
				Controller.getCurrentController().getMapViewManager().addMapViewChangeListener(listener);
				final boolean disableCacheProperty = ResourceController.getResourceController().getBooleanProperty(
				    FORMULA_DISABLE_CACHING);
				if (disableCacheProperty) {
					System.err.println("Formula cache disabled."
					        + " This might severely impair performance when using formulas.");
				}
			}
			else {
				System.out.println("Formula plugin is disabled");
			}
		}

		private void addMenuItems(ModeController modeController) {
			final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
			addSubMenu(menuBuilder, MENU_BAR_PARENT_LOCATION, MENU_BAR_LOCATION,
			    FormulaUtils.getFormulaKey("menuname"));
			menuBuilder.addAnnotatedAction(new EvaluateAllAction());
		}

		private void addSubMenu(final MenuBuilder menuBuilder, final String parentLocation,
		                        final String location, final String menuKey) {
			final JMenu menuItem = new JMenu();
			MenuBuilder.setLabelAndMnemonic(menuItem, TextUtils.getText(menuKey));
			menuBuilder.addMenuItem(parentLocation, menuItem, location, MenuBuilder.AS_CHILD);
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
		context.registerService(IModeControllerExtensionProvider.class.getName(), new FormulaPluginRegistration(),
		    props);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
	}
}
