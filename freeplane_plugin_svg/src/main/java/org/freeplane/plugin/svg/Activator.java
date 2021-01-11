package org.freeplane.plugin.svg;

import java.util.Hashtable;

import org.freeplane.core.ui.CaseSensitiveFileNameExtensionFilter;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.export.mindmapmode.ExportController;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.freeplane.view.swing.features.filepreview.ViewerController;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		registerMindMapModeExtension(context);
	}

	private void registerMindMapModeExtension(final BundleContext context) {
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		props.put("mode", new String[] { MModeController.MODENAME });
		context.registerService(IModeControllerExtensionProvider.class.getName(),
		    new IModeControllerExtensionProvider() {
			    public void installExtension(ModeController modeController) {
			    	final ExportController exportController = ExportController.getController(modeController);
			    	exportController.addMapExportEngine(new CaseSensitiveFileNameExtensionFilter("pdf", TextUtils.getText("export_pdf_text")), new ExportPdf());
			    	exportController.addMapExportEngine(new CaseSensitiveFileNameExtensionFilter("svg", TextUtils.getText("export_svg_text")), new ExportSvg());
				    final ViewerController extension = (ViewerController) modeController
				        .getExtension(ViewerController.class);
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
