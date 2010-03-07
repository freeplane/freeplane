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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileFilter;

import org.freeplane.core.addins.NodeHookDescriptor;
import org.freeplane.core.addins.PersistentNodeHook;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.INodeViewLifeCycleListener;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.HtmlTools;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.mindmapmode.file.MFileManager;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

@NodeHookDescriptor(hookName = "ExternalObject", //
onceForMap = false)
@ActionLocationDescriptor(locations = {"/menu_bar/insert/other", "/node_popup/insert/image"})
public class ViewerController extends PersistentNodeHook implements INodeViewLifeCycleListener, IExtension{
	
	private final class CombiFactory implements IViewerFactory {
	    private IViewerFactory factory;

	    public JComponent createViewer(URI uri, final Dimension preferredSize) throws MalformedURLException, IOException {
	    	factory = getViewerFactory(uri);
	        return factory == null ? null : factory.createViewer(uri, preferredSize);
	    }

	    public JComponent createViewer(ExternalResource resource, URI absoluteUri) throws MalformedURLException, IOException {
	    	factory = getViewerFactory(absoluteUri);
	        return factory.createViewer(resource, absoluteUri);
	    }

	    public String getDescription() {
	    	StringBuilder sb = new StringBuilder();
	    	for(IViewerFactory factory:factories){
	    		if(sb.length() != 0){
	    			sb.append(", ");
	    		}
	    		sb.append(factory.getDescription());
	    	}
	    	return sb.toString();
	    }

	    public Dimension getOriginalSize(JComponent viewer) {
	        return factory.getOriginalSize(viewer);
	    }

	    public void setViewerSize(JComponent viewer, Dimension size) {
	        factory.setViewerSize(viewer, size);
	    }

	    public boolean accept(URI uri) {
	        return getViewerFactory(uri) != null;
	    }
    }
	static final class FactoryFileFilter extends FileFilter {
	    private final IViewerFactory factory;

	    protected IViewerFactory getFactory() {
        	return factory;
        }

		private FactoryFileFilter(IViewerFactory factory) {
		    this.factory = factory;
	    }

	    @Override
	    public boolean accept(File f) {
	        return f.isDirectory() || factory.accept(f.toURI());
	    }

	    @Override
	    public String getDescription() {
	        return factory.getDescription();
	    }
    }
	private class MyMouseListener implements MouseListener, MouseMotionListener{
		private boolean isActive = false;
		private boolean sizeChanged = false;
		
		public void mouseClicked(MouseEvent e) {
			if(resetSize(e)){
				return;
			}
			if(showPopupMenu(e)){
				return;
			}
			if(openUri(e)){
				return;
			}
		}

		private boolean openUri(MouseEvent e) {
			if(e.getClickCount() != 2){
				return false;
			}
			final ExternalResource model = getModel(e);
			if(model == null){
				return true;
			}
			final UrlManager urlManager = (UrlManager) getModeController().getExtension(UrlManager.class);
			urlManager.loadURL(model.getUri());
	        return true;
        }

		private boolean showPopupMenu(MouseEvent e) {
			return false;
		}

		private boolean resetSize(MouseEvent e) {
			if(e.getClickCount() != 2 ){
				return false;
			}
			JComponent viewer = (JComponent) e.getComponent();
			final int x = e.getX();
			int width = viewer.getWidth();
			final int y = e.getY();
			int height = viewer.getHeight();
			if(x < width- 4 * BORDER_SIZE || y < height- 4 * BORDER_SIZE){
				return false;
			}
			IViewerFactory factory = (IViewerFactory) viewer.getClientProperty(IViewerFactory.class);
			if(factory == null){
				return true;
			}
			MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, viewer);
			setZoom(mapView.getModeController(), mapView.getModel(),
					(ExternalResource) viewer
							.getClientProperty(ExternalResource.class),
					1f);
			sizeChanged = false;
			return true;
		}

		public void mouseEntered(MouseEvent e) {
			if(isActive){
				return;
			}
			final ExternalResource model = getModel(e);
			if(model == null){
				return;
			}
			getController().getViewController().out(model.getUri().toString());
			setCursor(e);
		}

		private ExternalResource getModel(MouseEvent e) {
	        final JComponent component = (JComponent) e.getComponent();
			final ExternalResource model = (ExternalResource) component.getClientProperty(ExternalResource.class);
	        return model;
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
			final int y = e.getY();
			int height = component.getHeight();
			if(width- 2 * BORDER_SIZE <= x && x <= width && height- 2 * BORDER_SIZE <= y && y <= height){
				cursorType = Cursor.SE_RESIZE_CURSOR;
			}
			else{
				cursorType = Cursor.DEFAULT_CURSOR;
			}
			Cursor cursor = component.getCursor();
			if(cursor.getType() != cursorType){
				Cursor predefinedCursor = cursorType == Cursor.DEFAULT_CURSOR ? null : Cursor.getPredefinedCursor(cursorType);
				component.setCursor(predefinedCursor);
			}
			
			
		}

		public void mousePressed(MouseEvent e) {
			final JComponent component = (JComponent) e.getComponent();
			int cursorType = component.getCursor().getType();
			if(cursorType != Cursor.SE_RESIZE_CURSOR){
				return;
			}
			final IViewerFactory factory = (IViewerFactory) component.getClientProperty(IViewerFactory.class);
			if(factory == null){
				return;
			}
			isActive = true;
		}

		public void mouseReleased(MouseEvent e) {
			if(sizeChanged){
				JComponent component = (JComponent) e.getComponent();
				int x = component.getWidth();
				int y = component.getHeight();
				final IViewerFactory factory = (IViewerFactory) component.getClientProperty(IViewerFactory.class);
				double r = Math.sqrt(x*x+y*y);
				final Dimension originalSize = factory.getOriginalSize(component);
				int w = originalSize.width;
				int h = originalSize.height;
				double r0 = Math.sqrt(w*w+h*h);
				MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, component);
				float zoom = mapView.getZoom();
				final float modelSize = (float)(r / r0 /zoom);
				setZoom(mapView.getModeController(), mapView.getModel(),
					(ExternalResource) component
					.getClientProperty(ExternalResource.class),
					modelSize);
				sizeChanged = false;
			}
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
			sizeChanged = true;
			final Dimension size;
			switch(cursorType){
			case Cursor.SE_RESIZE_CURSOR:
				Dimension minimumSize = new Dimension(10, 10);
				int x = e.getX()-2*BORDER_SIZE;
				int y = e.getY()-2*BORDER_SIZE;
				if(x <= 0 || y <= 0){
					return true;
				}
				double r = Math.sqrt(x*x+y*y);
				Dimension preferredSize = factory.getOriginalSize(component);
				int width = preferredSize.width;
				int height = preferredSize.height;
				double r0 = Math.sqrt(width*width+height*height);
				x = (int)(width*r/r0);
				y = (int)(height*r/r0);
				MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, component);
				if(x < mapView.getZoomed(minimumSize.width) || y < mapView.getZoomed(minimumSize.height)){
					return true;
				}
				size = new Dimension( x, y);
				factory.setViewerSize(component, size );
				component.revalidate();
				break;
			default:
			}
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

	private final MyMouseListener mouseListener = new MyMouseListener();

final private Set<IViewerFactory> factories;	
	public ViewerController(ModeController modeController) {
		super(modeController);
		factories = new HashSet<IViewerFactory>();
		modeController.addINodeViewLifeCycleListener(this);
		modeController.addExtension(this.getClass(), this);
		factories.add(new BitmapViewerFactory());
	}

	public void setZoom(final ModeController modeController, final MapModel map, final ExternalResource model, final float size) {
		final float oldSize = model.getZoom();
		if(size == oldSize){
			return;
		}
		IActor actor = new IActor(){

			public void act() {
				model.setZoom(size);
				modeController.getMapController().setSaved(map, false);
			}

			public String getDescription() {
				return "setModelSize";
			}

			public void undo() {
				model.setZoom(oldSize);
				modeController.getMapController().setSaved(map, false);
			}};
			modeController.execute(actor, map);
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
		Controller controller = getController();
		ViewController viewController = controller.getViewController();
		MapModel map = node.getMap();
		File file = map.getFile();
		boolean useRelativeUri = ResourceController.getResourceController().getProperty("links").equals("relative");
		if (file == null && useRelativeUri) {
			JOptionPane.showMessageDialog(viewController.getContentPane(), ResourceBundles
			    .getText("not_saved_for_image_error"), "Freeplane", JOptionPane.WARNING_MESSAGE);
			return null;
		}
		UrlManager urlManager = (UrlManager) getModeController().getExtension(UrlManager.class);
		JFileChooser chooser = urlManager.getFileChooser(null);
		chooser.setAcceptAllFileFilterUsed(false);
		if(factories.size() > 1){
			final FileFilter combiFileFilter = getCombiFileFilter();
			chooser.addChoosableFileFilter(combiFileFilter);
			for(IViewerFactory factory:factories){
				chooser.addChoosableFileFilter(new FactoryFileFilter(factory));
			}
			chooser.setFileFilter(combiFileFilter);
		}
		else{
			chooser.setFileFilter(new FactoryFileFilter(factories.iterator().next()));
		}
		chooser.setAccessory(new ImagePreview(chooser));
		final int returnVal = chooser.showOpenDialog(getController().getViewController().getContentPane());
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		final File input = chooser.getSelectedFile();
		if (input == null) {
			return null;
		}
		URI uri = input.toURI();
		if (uri == null) {
			return null;
		}
		if (useRelativeUri) {
			uri = LinkController.toRelativeURI(map.getFile(), input);
		}
		ExternalResource preview = new ExternalResource();
		preview.setUri(uri);
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
			String attrUri = element.getAttribute("URI", null);
			if(attrUri != null){
				URI uri= new URI(attrUri);
				previewUrl.setUri(uri);
			}
			String attrSize = element.getAttribute("SIZE", null);
			if(attrSize != null){
				float size = Float.parseFloat(attrSize);
				previewUrl.setZoom(size);
			}
			getModeController().getMapController().nodeChanged(node);
		} catch (URISyntaxException e) {
		}
		return previewUrl;
	}

	void createViewer(final ExternalResource model, final NodeView view) {
			JComponent viewer = createViewer(view.getMap().getModel(), model);
			viewer.setBorder(new MatteBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_COLOR));
			final Set<JComponent> viewers = model.getViewers();
			viewers.add(viewer);
			view.getContentPane().add(viewer);
			if(model.getZoom() != -1){
			}
			viewer.revalidate();
			viewer.repaint();
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
		float size = previewUri.getZoom();
		if(size != -1){
			element.setAttribute("SIZE", Float.toString(size));
		}
		super.saveExtension(extension, element);
	}

	private JComponent createViewer(final MapModel map, final ExternalResource model) {
		final URI uri = model.getUri();
		if(uri == null ){
			return new JLabel("no file set");
		}
		final URI absoluteUri = model.getAbsoluteUri(map, getModeController());
		IViewerFactory factory = getViewerFactory(absoluteUri);
		if(factory == null){
			return new JLabel(uri.toString());
		}
		JComponent viewer = null;
        try {
	        viewer = factory.createViewer(model, absoluteUri);
        }
        catch (Exception e) {
        	String info = HtmlTools.combineTextWithExceptionInfo(uri.toString(), e);
			return new JLabel(info);
       }
		if(viewer == null){
			return new JLabel(uri.toString());
		}
		viewer.putClientProperty(IViewerFactory.class,factory);
		viewer.putClientProperty(ExternalResource.class,model);
		viewer.addMouseListener(mouseListener);
		viewer.addMouseMotionListener(mouseListener);
		return viewer;
	}

	private FileFilter getCombiFileFilter() {
		return new FactoryFileFilter(new CombiFactory());
	}


	public void addFactory(IViewerFactory factory){
		factories.add(factory);
	}
	public void removeFactory(IViewerFactory factory){
		factories.remove(factory);
	}
}
