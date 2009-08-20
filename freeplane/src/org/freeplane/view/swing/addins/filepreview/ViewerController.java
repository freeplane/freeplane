package org.freeplane.view.swing.addins.filepreview;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;

import org.freeplane.core.addins.NodeHookDescriptor;
import org.freeplane.core.addins.PersistentNodeHook;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.modecontroller.INodeViewLifeCycleListener;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.url.UrlManager;
import org.freeplane.features.mindmapmode.file.MFileManager;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.view.swing.map.NodeView;

@NodeHookDescriptor(hookName = "PreviewUri", //
onceForMap = false)
@ActionLocationDescriptor(locations = "/menu_bar/insert/other")
public class ViewerController extends PersistentNodeHook implements IViewerFactory, INodeViewLifeCycleListener, IExtension{

final private Set<IViewerFactory> factories;	
	public ViewerController(ModeController modeController) {
		super(modeController);
		factories = new HashSet<IViewerFactory>();
		modeController.addINodeViewLifeCycleListener(this);
		modeController.addExtension(this.getClass(), this);
	}

	@Override
	protected void add(final NodeModel node, final IExtension extension) {
		final ExternalResource preview = (ExternalResource) extension;
		final Iterator iterator = node.getViewers().iterator();
		while (iterator.hasNext()) {
			final NodeView view = (NodeView) iterator.next();
			createViewer(preview, view);
		}
		super.add(node, extension);
	}

	protected IExtension createExtension(final NodeModel node) {
		final URI uri = ((MFileManager) UrlManager.getController(getModeController()))
		.getLinkByFileChooser(getController().getMap(), getFileFilter());
		if (uri == null) {
			return null;
		}
		ExternalResource preview = new ExternalResource();
		preview.setUri(uri, this);
		return preview;
	}

	@Override
	protected IExtension createExtension(NodeModel node, XMLElement element) {
		ExternalResource previewUrl = new ExternalResource();
		try {
			String attribute = element.getAttribute("URI", null);
			if(attribute != null){
				previewUrl.setUri(new URI(attribute), this);
			}
			getModeController().getMapController().nodeChanged(node);
		} catch (URISyntaxException e) {
		}
		return previewUrl;
	}

	void createViewer(final ExternalResource model, final NodeView view) {
		try {
			URI uri = model.getUri();
			UrlManager urlManager = (UrlManager) getModeController().getExtension(UrlManager.class);
			URL absoluteUrl = urlManager.getAbsoluteUrl(view.getModel().getMap(), uri);
			final JComponent comp = createViewer(new File(absoluteUrl.getFile()));
			final Set<JComponent> viewers = model.getViewers();
			viewers.add(comp);
			view.getContentPane().add(comp);
			comp.revalidate();
			comp.repaint();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	void deleteViewer(final ExternalResource model, final NodeView nodeView) {
		final Set<JComponent> viewers = model.getViewers();
		if (viewers.isEmpty()) {
			return;
		}
		final Container contentPane = nodeView.getContentPane();
		final int componentCount = contentPane.getComponentCount();
		for (int i = 0; i < componentCount; i++) {
			final Component component = contentPane.getComponent(i);
			if (viewers.contains(component)) {
				viewers.remove(component);
				contentPane.remove(i);
				return;
			}
		}
	}

	@Override
	protected Class getExtensionClass() {
		return ExternalResource.class;
	}

	public void onViewCreated(final Container container) {
		final NodeView nodeView = (NodeView) container;
		final ExternalResource previewUri = (ExternalResource) nodeView.getModel().getExtension(ExternalResource.class);
		if (previewUri == null) {
			return;
		}
		createViewer(previewUri, nodeView);
	}

	public void onViewRemoved(final Container container) {
		final NodeView nodeView = (NodeView) container;
		final ExternalResource previewUri = (ExternalResource) nodeView.getModel().getExtension(ExternalResource.class);
		if (previewUri == null) {
			return;
		}
		deleteViewer(previewUri, nodeView);
	}

	@Override
	protected void remove(final NodeModel node, final IExtension extension) {
		final ExternalResource latexExtension = (ExternalResource) extension;
		latexExtension.removeViewers();
		super.remove(node, extension);
	}

	@Override
	protected void saveExtension(final IExtension extension, final XMLElement element) {
		final ExternalResource previewUri = (ExternalResource) extension;
		URI uri = previewUri.getUri();
		if(uri != null){
			element.setAttribute("URI", uri.toString());
		}
		super.saveExtension(extension, element);
	}

	void setUriUndoable(final ExternalResource model, final URI newUri) {
		final URI uri = model.getUri();
		if (uri.equals(newUri)) {
			return;
		}
		final IActor actor = new IActor() {
			private final URI oldUri = uri;

			public void act() {
				model.setUri(newUri, ViewerController.this);
				final MapModel map = getModeController().getController().getMap();
				getModeController().getMapController().setSaved(map, false);
			}

			public String getDescription() {
				return "setUriUndoable";
			}

			public void undo() {
				model.setUri(oldUri, ViewerController.this);
			}
		};
		getModeController().execute(actor, getModeController().getController().getMap());
	}
	public JComponent createViewer(File file) {
		if(file == null ){
			return new JLabel("no file set");
		}
		for(IViewerFactory factory:factories){
			if(factory.getFileFilter().accept(file)){
				return factory.createViewer(file);
			}
		}
		return new JLabel(file.toString());
	}

	public FileFilter getFileFilter() {
		return new FileFilter(){

			public boolean accept(File pathname) {
				if (pathname.isDirectory()){
					return true;
				}
				for(IViewerFactory factory:factories){
					if(factory.getFileFilter().accept(pathname)){
						return true;
					}
				}
				return false;
			}

			@Override
			public String getDescription() {
				StringBuilder sb = new StringBuilder();
				for(IViewerFactory factory:factories){
					if(sb.length() != 0){
						sb.append(", ");
					}
					sb.append(factory.getFileFilter().getDescription());
				}
				return sb.toString();
				
			}};
	}

	public void addFactory(IViewerFactory factory){
		factories.add(factory);
	}
	public void removeFactory(IViewerFactory factory){
		factories.remove(factory);
	}
}
