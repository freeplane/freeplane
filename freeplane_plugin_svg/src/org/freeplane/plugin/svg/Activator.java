package org.freeplane.plugin.svg;

import java.util.Hashtable;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.browsemode.BModeController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.freeplane.view.swing.addins.filepreview.ViewerController;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		registerMindMapModeExtension(context);
		registerBrowseModeExtension(context);
	}

	private void registerMindMapModeExtension(final BundleContext context) {
	    final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		props.put("mode", new String[] { MModeController.MODENAME });
		context.registerService(IModeControllerExtensionProvider.class.getName(),
		    new IModeControllerExtensionProvider() {
			    public void installExtension(final ModeController modeController) {
				    final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
				    final Controller controller = modeController.getController();
				    menuBuilder.addAnnotatedAction(new ExportPdf(controller));
				    menuBuilder.addAnnotatedAction(new ExportSvg(controller));
				    ViewerController extension = (ViewerController) modeController.getExtension(ViewerController.class);
				    extension.addFactory(new SvgViewerFactory());
			    }
		    }, props);
    }

	private void registerBrowseModeExtension(final BundleContext context) {
	    final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		props.put("mode", new String[] { BModeController.MODENAME });
		context.registerService(IModeControllerExtensionProvider.class.getName(),
		    new IModeControllerExtensionProvider() {
			    public void installExtension(final ModeController modeController) {
				    ViewerController extension = (ViewerController) modeController.getExtension(ViewerController.class);
				    extension.addFactory(new SvgViewerFactory());
			    }
		    }, props);
    }

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
	}
}
