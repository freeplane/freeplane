package org.freeplane.plugin.workspace;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.freeplane.core.resources.components.ComboProperty;
import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.IPropertyControlCreator;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

public class Activator implements BundleActivator {

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {		
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		props.put("mode", new String[] { MModeController.MODENAME });
		registerClasspathUrlHandler(context);		
		context.registerService(IModeControllerExtensionProvider.class.getName(),
		    new IModeControllerExtensionProvider() {
				public void installExtension(ModeController modeController) {
			    	registerLinkTypeOption();
				    WorkspaceController.getController().initialStart();
			    }
		    }, props);
	}
	
	private void registerClasspathUrlHandler(final BundleContext context) {
		Hashtable<String, String[]> properties = new Hashtable<String, String[]>();
        properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { WorkspaceController.WORKSPACE_RESOURCE_URL_PROTOCOL });
        context.registerService(URLStreamHandlerService.class.getName(), new WorkspaceUrlHandler(), properties);
        
        properties = new Hashtable<String, String[]>();
        properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { WorkspaceController.PROPERTY_RESOURCE_URL_PROTOCOL });
        context.registerService(URLStreamHandlerService.class.getName(), new PropertyUrlHandler(), properties);
    }
	
	private void registerLinkTypeOption() {
		IndexedTree.Node node = (IndexedTree.Node) MModeController.getMModeController().getOptionPanelBuilder().getRoot();
		IndexedTree.Node found = getNodeForPath("Environment/hyperlink_types/links", node);
		found.setUserObject(new OptionPanelExtender((IPropertyControlCreator) found.getUserObject()));		
		System.out.println(found);
	}
	
	private IndexedTree.Node getNodeForPath(String path, IndexedTree.Node node) {
		Enumeration<?> children = node.children();
		while(children.hasMoreElements()) {
			IndexedTree.Node child = (IndexedTree.Node)children.nextElement();
			if(child.getKey() != null && path.startsWith(child.getKey().toString())) {
				if(path.equals(child.getKey().toString())) {
					return child;
				}
				return getNodeForPath(path, child);
				
			}
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {		
	}
	
	
	private class OptionPanelExtender implements IPropertyControlCreator {
		private final IPropertyControlCreator creator;
		
		public OptionPanelExtender(final IPropertyControlCreator creator) {
			this.creator = creator;
		}

		public IPropertyControl createControl() {
			ComboProperty property = (ComboProperty) creator.createControl();
			List<String> list = property.getPossibleValues();
			list.add(WorkspacePreferences.RELATIVE_TO_WORKSPACE);
			return new ComboProperty(WorkspacePreferences.LINK_PROPERTY_KEY, list.toArray(new String[] {}));
		}

	}

}
