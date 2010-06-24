package org.freeplane.plugin.spreadsheet;

import java.net.URL;
import java.util.Hashtable;

import javax.swing.JMenu;

import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	static final String MENU_BAR_LOCATION = "/menu_bar/extras/first/spreadsheet";

	private final class SpreadsheetRegistration implements IModeControllerExtensionProvider {
		private MModeController modeController;

		public void installExtension(final ModeController modeController) {
			this.modeController = (MModeController) modeController;
			addMenuItems(modeController);
		}

		private void addMenuItems(final ModeController modeController) {
			final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
			addSubMenu(menuBuilder, "/menu_bar/extras/first", MENU_BAR_LOCATION, SpreadSheetUtils
			    .getSpreadSheetKey("ExecuteScripts"));
			menuBuilder.addAnnotatedAction(new EvaluateAllAction(modeController.getController()));
			addPropertiesToOptionPanel();
		}

		private void addSubMenu(final MenuBuilder menuBuilder, final String scriptsParentLocation,
		                        final String scriptsLocation, final String baseKey) {
			final JMenu menuItem = new JMenu();
			MenuBuilder.setLabelAndMnemonic(menuItem, baseKey + ".text");
			menuItem.setToolTipText(baseKey + ".tooltip");
			menuBuilder.addMenuItem(scriptsParentLocation, menuItem, scriptsLocation, MenuBuilder.AS_CHILD);
		}

		private void addPropertiesToOptionPanel() {
			final URL preferences = this.getClass().getResource("preferences.xml");
			if (preferences == null)
				throw new RuntimeException("cannot open preferences");
			modeController.getOptionPanelBuilder().load(preferences);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		props.put("mode", new String[] { MModeController.MODENAME /*TODO: browse mode too?*/});
		context.registerService(IModeControllerExtensionProvider.class.getName(), new SpreadsheetRegistration(), props);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
	}
}
