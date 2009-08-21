package org.freeplane.view.swing.addins.filepreview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
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
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

@NodeHookDescriptor(hookName = "ExternalObject", //
onceForMap = false)
@ActionLocationDescriptor(locations = "/menu_bar/insert/other")
public class ViewerController extends PersistentNodeHook implements INodeViewLifeCycleListener, IExtension{
	
	static private class MyMouseListener implements MouseListener, MouseMotionListener{
		private boolean isActive = false;
		public void mouseClicked(MouseEvent e) {
			if(resetSize(e)){
				return;
			}
			if(showPopupMenu(e)){
				return;
			}
		}

		private boolean showPopupMenu(MouseEvent e) {
			return false;
		}

		private boolean resetSize(MouseEvent e) {
			if(e.getClickCount() != 2 ){
				return false;
			}
			JComponent viewer = (JComponent) e.getComponent();
			int cursorType = viewer.getCursor().getType();
			if(cursorType != Cursor.E_RESIZE_CURSOR && cursorType != Cursor.S_RESIZE_CURSOR ){
				return false;
			}
			IViewerFactory factory = (IViewerFactory) viewer.getClientProperty(IViewerFactory.class);
			if(factory == null){
				return true;
			}
			final Dimension size = factory.getOriginalSize(viewer);
			MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, viewer);
			size.width = mapView.getZoomed(size.width);
			size.height = mapView.getZoomed(size.height);
			factory.setViewerSize(viewer, size );
			viewer.revalidate();
			return true;
		}

		public void mouseEntered(MouseEvent e) {
			setCursor(e);
		}

		public void mouseExited(MouseEvent e) {
			if(isActive){
				return;
			}
			setCursor(e);
		}

		private void setCursor(MouseEvent e) {
			Component component = e.getComponent();
			final int cursorType;
			final int x = e.getX();
			int width = component.getWidth();
			if(width- BORDER_SIZE <= x && x <= width){
				cursorType = Cursor.E_RESIZE_CURSOR;
			}
			else{
				final int y = e.getY();
				int height = component.getHeight();
				if(height- BORDER_SIZE <= y && y <= height){
					cursorType = Cursor.S_RESIZE_CURSOR;
				}
				else{
					cursorType = Cursor.DEFAULT_CURSOR;
				}
			}
			Cursor cursor = component.getCursor();
			if(cursor.getType() != cursorType){
				component.setCursor(Cursor.getPredefinedCursor(cursorType));
			}
			
			
		}

		public void mousePressed(MouseEvent e) {
			int cursorType = e.getComponent().getCursor().getType();
			if(cursorType != Cursor.E_RESIZE_CURSOR && cursorType != Cursor.S_RESIZE_CURSOR){
				return;
			}
			isActive = true;
		}

		public void mouseReleased(MouseEvent e) {
			isActive = false;
			setCursor(e);
		}

		public void mouseDragged(MouseEvent e) {
			if(! isActive){
				return;
			}
			setSize(e);
		}

		private boolean setSize(MouseEvent e) {
			if(! isActive){
				return false;
			}
			JComponent component = (JComponent) e.getComponent();
			int cursorType = component.getCursor().getType();
			IViewerFactory factory = (IViewerFactory) component.getClientProperty(IViewerFactory.class);
			if(factory == null){
				return true;
			}
			final Dimension size;
			switch(cursorType){
			case Cursor.E_RESIZE_CURSOR:
				size = new Dimension( e.getX(), component.getHeight());
				break;
			case Cursor.S_RESIZE_CURSOR:
				size = new Dimension( component.getWidth(), e.getY());
				break;
			default:
				size = null;
			}
			factory.setViewerSize(component, size );
			component.revalidate();
			return true;
		}

		public void mouseMoved(MouseEvent e) {
			if(isActive){
				return;
			}
			setCursor(e);
		}
		
	}

	private static final int BORDER_SIZE = 2;

	private static final Color BORDER_COLOR = Color.GRAY;

	private static final MyMouseListener mouseListener = new MyMouseListener();

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
		preview.setUri(uri, getViewerFactory(uri));
		return preview;
	}

	private IViewerFactory getViewerFactory(URI uri) {
		for(IViewerFactory factory:factories){
			if(factory.accept(uri)){
				return factory;
			}
		}
		return null;
	}

	@Override
	protected IExtension createExtension(NodeModel node, XMLElement element) {
		ExternalResource previewUrl = new ExternalResource();
		try {
			String attribute = element.getAttribute("URI", null);
			if(attribute != null){
				URI uri= new URI(attribute);
				previewUrl.setUri(uri, getViewerFactory(uri));
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
			URI absoluteUri = urlManager.getAbsoluteUri(view.getModel().getMap(), uri);
			JComponent comp;
			comp = createBorderedViewer(absoluteUri);
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
				model.setUri(newUri, getViewerFactory(newUri));
				final MapModel map = getModeController().getController().getMap();
				getModeController().getMapController().setSaved(map, false);
			}

			public String getDescription() {
				return "setUriUndoable";
			}

			public void undo() {
				model.setUri(oldUri, getViewerFactory(oldUri));
			}
		};
		getModeController().execute(actor, getModeController().getController().getMap());
	}
	public JComponent createBorderedViewer(URI uri) {
		JComponent viewer = createViewer(uri);
		viewer.setBorder(new MatteBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_COLOR));
		return viewer;
	}

	private JComponent createViewer(URI uri) {
		if(uri == null ){
			return new JLabel("no file set");
		}
		IViewerFactory factory = getViewerFactory(uri);
		if(factory != null){
			JComponent viewer = factory.createViewer(uri);
			viewer.putClientProperty(IViewerFactory.class,factory);
//			viewer.addMouseListener(mouseListener);
//			viewer.addMouseMotionListener(mouseListener);
			return viewer;
		}
		return new JLabel(uri.toString());
	}

	public FileFilter getFileFilter() {
		return new FileFilter(){

			public boolean accept(File pathname) {
				if (pathname.isDirectory()){
					return true;
				}
				return getViewerFactory(pathname.toURI()) != null;
			}

			@Override
			public String getDescription() {
				StringBuilder sb = new StringBuilder();
				for(IViewerFactory factory:factories){
					if(sb.length() != 0){
						sb.append(", ");
					}
					sb.append(factory.getDescription());
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
