package org.freeplane.view.swing.features.filepreview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.dnd.DropTarget;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileFilter;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.INodeView;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.features.ui.INodeViewLifeCycleListener;
import org.freeplane.features.ui.ViewController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.view.swing.features.progress.mindmapmode.ProgressIcons;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

@NodeHookDescriptor(hookName = "ExternalObject", //
onceForMap = false)
public class ViewerController extends PersistentNodeHook implements INodeViewLifeCycleListener, IExtension {
	private static final MExternalImageDropListener DTL = new MExternalImageDropListener();
	private static final int BORDER_SIZE = 1;
	public static final Border VIEWER_BORDER_INSTANCE = new ViewerBorder(BORDER_SIZE, Color.BLACK);

	private final class CombiFactory implements IViewerFactory {
		private IViewerFactory factory;

		public ScalableComponent createViewer(final URI uri,
				final Dimension preferredSize) throws MalformedURLException,
				IOException {
			factory = getViewerFactory(uri);
			ScalableComponent component = (factory == null ? null : factory.createViewer(uri,
					preferredSize));
			return component;
		}

		public ScalableComponent createViewer(final ExternalResource resource,
				final URI absoluteUri, final int maximumWidth)
		        throws MalformedURLException, IOException {
			factory = getViewerFactory(absoluteUri);
			ScalableComponent component = factory.createViewer(resource, absoluteUri,
					maximumWidth);
			return component;
		}

		public String getDescription() {
			final StringBuilder sb = new StringBuilder();
			for (final IViewerFactory factory : factories) {
				if (sb.length() != 0) {
					sb.append(", ");
				}
				sb.append(factory.getDescription());
			}
			return sb.toString();
		}

		public boolean accept(final URI uri) {
			return getViewerFactory(uri) != null;
		}

		public ScalableComponent createViewer(URI uri, float zoom)
				throws MalformedURLException, IOException {
			factory = getViewerFactory(uri);
			ScalableComponent component = (factory == null ? null : factory.createViewer(uri,
					zoom));
			return component;
		}

	}

	static final class FactoryFileFilter extends FileFilter {
		private final IViewerFactory factory;

		protected IViewerFactory getFactory() {
			return factory;
		}

		private FactoryFileFilter(final IViewerFactory factory) {
			this.factory = factory;
		}

		@Override
		public boolean accept(final File f) {
			return f.isDirectory() || factory.accept(f.toURI());
		}

		@Override
		public String getDescription() {
			return factory.getDescription();
		}
	}

	private class MyMouseListener implements MouseListener, MouseMotionListener {
		private boolean isActive = false;
		private boolean sizeChanged = false;

		public void mouseClicked(final MouseEvent e) {
			if (resetSize(e)) {
				return;
			}
			if (showNext(e)) {
				return;
			}
		}

		private boolean resetSize(final MouseEvent e) {
			if (e.getClickCount() != 2) {
				return false;
			}
			final JComponent viewer = (JComponent) e.getComponent();
			final int x = e.getX();
			final int width = viewer.getWidth();
			final int y = e.getY();
			final int height = viewer.getHeight();
			if (x < width - 4 * BORDER_SIZE || y < height - 4 * BORDER_SIZE) {
				return false;
			}
			final IViewerFactory factory = (IViewerFactory) viewer.getClientProperty(IViewerFactory.class);
			if (factory == null) {
				return true;
			}
			final MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, viewer);
			setZoom(mapView.getModeController(), mapView.getModel(), (ExternalResource) viewer
			    .getClientProperty(ExternalResource.class), 1f);
			sizeChanged = false;
			return true;
		}

		private boolean showNext(final MouseEvent e) {
			//double left click
			final JComponent component = (JComponent) e.getComponent();
			final int cursorType = component.getCursor().getType();
			if ((e.getClickCount() != 2) || (e.getButton() != MouseEvent.BUTTON1)
			        || (cursorType == Cursor.SE_RESIZE_CURSOR)) {
				return false;
			}
			final ExternalResource activeView = getModel(e);
			NodeModel node = null;
			//get node from mouse click
			for (int i = 0; i < e.getComponent().getParent().getComponentCount(); i++) {
				if (e.getComponent().getParent().getComponent(i) instanceof MainView) {
					final MainView mv = (MainView) e.getComponent().getParent().getComponent(i);
					node = mv.getNodeView().getModel();
					break;
				}
			}
			if (node == null) {
				node = Controller.getCurrentModeController().getMapController().getSelectedNode();
			}
			final MapModel map = node.getMap();
			URI absoluteUri = activeView.getAbsoluteUri(map);
			if(absoluteUri == null)
				return false;
			final String sActUri = absoluteUri.toString();
			if (!sActUri.matches(".*_[0-9]{2}\\.[a-zA-Z0-9]*")) {
				return false;
			}
			int i = Integer.parseInt(sActUri.substring(sActUri.lastIndexOf("_") + 1, sActUri.lastIndexOf("_") + 3));
			//show previous with ctrl + double click
			if (e.isControlDown()) {
				if (i > 0) {
					i--;
				}
				else {
					//remove view if 0 and down
					if (activeView.getUri().toString().matches(ProgressIcons.EXTENDED_PROGRESS_ICON_IDENTIFIER)) {
						ProgressIcons.removeProgressIcons(node);
					}
					remove(node, activeView);
					Controller.getCurrentModeController().getMapController().nodeChanged(node,
					    NodeModel.UNKNOWN_PROPERTY, null, null);
					return true;
				}
			}
			else {
				i++;
			}
			final String sNextNum;
			if (i < 10) {
				sNextNum = "0" + Integer.toString(i);
			}
			else {
				sNextNum = Integer.toString(i);
			}
			URI nextUri = null;
			try {
				nextUri = new URI(sActUri.replaceFirst("_[0-9]{2}\\.", "_" + sNextNum + "."));
			}
			catch (final URISyntaxException e1) {
				e1.printStackTrace();
			}
			final String sNextURI = nextUri.getPath();
			if ((sNextURI.contains("_tenth_")&& (i > 10))|| ((sNextURI.contains("_quarter_"))&& (i > 4))) {
				return false;
			}
			final ExternalResource nextView = new ExternalResource(nextUri);
			nextView.setZoom(activeView.getZoom());
			remove(node, activeView);
			add(node, nextView);
			ProgressIcons.updateExtendedProgressIcons(node, sNextURI);
			return true;
		}

		public void mouseEntered(final MouseEvent e) {
			if (isActive) {
				return;
			}
			final ExternalResource model = getModel(e);
			if (model == null) {
				return;
			}
			Controller.getCurrentController().getViewController().out(model.getUri().toString());
			setCursor(e);
		}

		private ExternalResource getModel(final MouseEvent e) {
			final JComponent component = (JComponent) e.getComponent();
			final ExternalResource model = (ExternalResource) component.getClientProperty(ExternalResource.class);
			return model;
		}

		public void mouseExited(final MouseEvent e) {
			if (isActive) {
				return;
			}
			setCursor(e.getComponent(), Cursor.DEFAULT_CURSOR);
		}

		private void setCursor(final MouseEvent e) {
			final Component component = e.getComponent();
			final int cursorType;
			final int x = e.getX();
			final int width = component.getWidth();
			final int y = e.getY();
			final int height = component.getHeight();
			if (x >= 0 && width - 6 * BORDER_SIZE <= x && x < width 
					&& y >= 0 && height - 6 * BORDER_SIZE <= y && y < height) {
				cursorType = Cursor.SE_RESIZE_CURSOR;
			}
			else {
				cursorType = Cursor.DEFAULT_CURSOR;
			}
			setCursor(component, cursorType);
		}

		private void setCursor(final Component component, final int cursorType) {
			final Cursor cursor = component.getCursor();
			if (cursor.getType() != cursorType) {
				final Cursor predefinedCursor = cursorType == Cursor.DEFAULT_CURSOR ? null : Cursor
				    .getPredefinedCursor(cursorType);
				component.setCursor(predefinedCursor);
			}
			ViewerBorder.repaintBorder((JComponent) component);
		}

		public void mousePressed(final MouseEvent e) {
			final JComponent component = (JComponent) e.getComponent();
			final int cursorType = component.getCursor().getType();
			if (cursorType == Cursor.SE_RESIZE_CURSOR) {
				final IViewerFactory factory = (IViewerFactory) component.getClientProperty(IViewerFactory.class);
				if (factory == null) {
					return;
				}
				isActive = true;
				return;
			}
			else {
				imagePopupMenu.maybeShowPopup(e);
				return;
			}
		}

		public void mouseReleased(final MouseEvent e) {
			if (sizeChanged) {
				final JComponent component = (JComponent) e.getComponent();
				final int x = component.getWidth();
				final int y = component.getHeight();
				final double r = Math.sqrt(x * x + y * y);
				final Dimension originalSize = ((ScalableComponent) component).getOriginalSize();
				final int w = originalSize.width;
				final int h = originalSize.height;
				final double r0 = Math.sqrt(w * w + h * h);
				final MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, component);
				final float zoom = mapView.getZoom();
				final float modelSize = (float) (r / r0 / zoom);
				setZoom(mapView.getModeController(), mapView.getModel(), (ExternalResource) component
				    .getClientProperty(ExternalResource.class), modelSize);
				sizeChanged = false;
			}
			else {
				imagePopupMenu.maybeShowPopup(e);
			}
			isActive = false;
			setCursor(e);
		}

		public void mouseDragged(final MouseEvent e) {
			if (!isActive) {
				return;
			}
			setSize(e);
		}

		private boolean setSize(final MouseEvent e) {
			if (!isActive) {
				return false;
			}
			final JComponent component = (JComponent) e.getComponent();
			final int cursorType = component.getCursor().getType();
			sizeChanged = true;
			final Dimension size;
			switch (cursorType) {
				case Cursor.SE_RESIZE_CURSOR:
					final Dimension minimumSize = new Dimension(10, 10);
					int x = e.getX() - 4 * BORDER_SIZE;
					int y = e.getY() - 4 * BORDER_SIZE;
					if (x <= 0 || y <= 0) {
						return true;
					}
					final double r = Math.sqrt(x * x + y * y);
					final Dimension preferredSize = ((ScalableComponent) component).getOriginalSize();
					final int width = preferredSize.width;
					final int height = preferredSize.height;
					final double r0 = Math.sqrt(width * width + height * height);
					x = (int) (width * r / r0);
					y = (int) (height * r / r0);
					final MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, component);
					if (x < mapView.getZoomed(minimumSize.width) || y < mapView.getZoomed(minimumSize.height)) {
						return true;
					}
					size = new Dimension(x, y);
					((ScalableComponent) component).setDraftViewerSize(size);
					component.revalidate();
					break;
				default:
			}
			return true;
		}

		public void mouseMoved(final MouseEvent e) {
			if (isActive) {
				return;
			}
			setCursor(e);
		}
	}

	static private ExternalImagePopupMenu imagePopupMenu;
	static final int VIEWER_POSITION = 5;
	private final MyMouseListener mouseListener = new MyMouseListener();
	final private Set<IViewerFactory> factories;

	public ViewerController() {
		super();
		factories = new HashSet<IViewerFactory>();
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addINodeViewLifeCycleListener(this);
		modeController.addExtension(this.getClass(), this);
		factories.add(new BitmapViewerFactory());
	}
	
	@Override
	protected HookAction createHookAction() {
		return null;
	}
	
	public void setZoom(final ModeController modeController, final MapModel map, final ExternalResource model,
	                    final float size) {
		final float oldSize = model.getZoom();
		if (size == oldSize) {
			return;
		}
		final IActor actor = new IActor() {
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
			}
		};
		modeController.execute(actor, map);
	}

	@Override
	protected void add(final NodeModel node, final IExtension extension) {
		final ExternalResource preview = (ExternalResource) extension;
		for(NodeModel nodeClone : node.allClones()){
			for (final INodeView iNodeView : nodeClone.getViewers()) {
				final NodeView view = (NodeView) iNodeView;
				createViewer(preview, view);
			}
		}
		super.add(node, extension);
	}

	@Override
	protected IExtension createExtension(final NodeModel node) {
		URI uri = createURI(node);
		if(uri == null)
			return null;
		File input = new File(uri.getPath());
		final ExternalResource preview = new ExternalResource(uri);
		ProgressIcons.updateExtendedProgressIcons(node, input.getName());
		return preview;
	}
	
	protected URI createURI(final NodeModel node) {
		final Controller controller = Controller.getCurrentController();
		final ViewController viewController = controller.getViewController();
		final MapModel map = node.getMap();
		final File file = map.getFile();
		final boolean useRelativeUri = ResourceController.getResourceController().getProperty("links").equals(
		    "relative");
		if (file == null && useRelativeUri) {
			JOptionPane.showMessageDialog(viewController.getCurrentRootComponent(), TextUtils
			    .getText("not_saved_for_image_error"), "Freeplane", JOptionPane.WARNING_MESSAGE);
			return null;
		}
		final UrlManager urlManager = controller.getModeController().getExtension(UrlManager.class);
		final JFileChooser chooser = urlManager.getFileChooser(null, false);
		chooser.setAcceptAllFileFilterUsed(false);
		final FileFilter fileFilter;
		if (factories.size() > 1) {
			fileFilter = getCombiFileFilter();
			chooser.addChoosableFileFilter(fileFilter);
			for (final IViewerFactory factory : factories) {
				chooser.addChoosableFileFilter(new FactoryFileFilter(factory));
			}
		}
		else {
			fileFilter = new FactoryFileFilter(factories.iterator().next());
		}
		chooser.setFileFilter(fileFilter);
		chooser.putClientProperty(FactoryFileFilter.class, fileFilter);
		chooser.setAccessory(new ImagePreview(chooser));
		final int returnVal = chooser.showOpenDialog(Controller.getCurrentController().getViewController()
		    .getCurrentRootComponent());
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		final File input = chooser.getSelectedFile();
		if (input == null) {
			return null;
		}
		URI uri = uriOf(input);
		if (uri == null) {
			return null;
		}
		if (useRelativeUri && uri.getScheme().equals("file")) {
			uri = LinkController.toLinkTypeDependantURI(map.getFile(), input);
		}
		return uri;
	}


	private URI uriOf(final File input) {
		String path = input.getPath();
		try {
	        for (String protocol : new String[]{"http:" + File.separatorChar, "https:" + File.separatorChar}){
	        	int uriStart = path.indexOf(protocol);
	        	if(uriStart != -1)
	        		return new URI(protocol.substring(0, protocol.length() - 1) + "//" + path.substring(uriStart + protocol.length()).replace('\\', '/'));
	        }
        }
        catch (URISyntaxException e) {
        	LogUtils.warn(e);
        }
	    return input.toURI();
    }

	private IViewerFactory getViewerFactory(final URI uri) {
		for (final IViewerFactory factory : factories) {
			if (factory.accept(uri)) {
				return factory;
			}
		}
		return null;
	}

	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		try {
			final String attrUri = element.getAttribute("URI", null);
			if (attrUri != null) {
				final URI uri = new URI(attrUri);
				final ExternalResource previewUrl = new ExternalResource(uri);
				final String attrSize = element.getAttribute("SIZE", null);
				if (attrSize != null) {
					final float size = Float.parseFloat(attrSize);
					previewUrl.setZoom(size);
				}
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
				return previewUrl;
			}
		}
		catch (final URISyntaxException e) {
		}
		return null;
	}

	void createViewer(final ExternalResource model, final NodeView view) {
		final JComponent viewer = createViewer(view.getMap().getModel(), model);
		if (imagePopupMenu == null) {
			imagePopupMenu = new ExternalImagePopupMenu();
		}
		viewer.setBorder(VIEWER_BORDER_INSTANCE);
		final Set<NodeView> viewers = model.getViewers();
		viewers.add(view);
		viewer.setBounds(viewer.getX() - 5, viewer.getY() - 5, viewer.getWidth() + 15, viewer.getHeight() + 15);
		view.addContent(viewer, VIEWER_POSITION);
		if(view.getMap().getModeController().canEdit()){
			final DropTarget dropTarget = new DropTarget(viewer, DTL);
			dropTarget.setActive(true);
		}
		if(view.isShortened())
			viewer.setVisible(false);
		else {
			viewer.revalidate();
			viewer.repaint();
		}
	}

	void deleteViewer(final ExternalResource model, final NodeView nodeView) {
		final Set<NodeView> viewers = model.getViewers();
		if (!viewers.contains(nodeView)) {
			return;
		}
		nodeView.removeContent(VIEWER_POSITION);
		viewers.remove(nodeView);
	}

	@Override
	protected Class<ExternalResource> getExtensionClass() {
		return ExternalResource.class;
	}

	public void onViewCreated(final Container container) {
		final NodeView nodeView = (NodeView) container;
		final ExternalResource previewUri = nodeView.getModel().getExtension(ExternalResource.class);
		if (previewUri == null) {
			return;
		}
		createViewer(previewUri, nodeView);
	}

	public void onViewRemoved(final Container container) {
		final NodeView nodeView = (NodeView) container;
		final ExternalResource previewUri = nodeView.getModel().getExtension(ExternalResource.class);
		if (previewUri == null) {
			return;
		}
		deleteViewer(previewUri, nodeView);
	}

	@Override
	protected void remove(final NodeModel node, final IExtension extension) {
		final ExternalResource resource = (ExternalResource) extension;
		resource.removeViewers();
		super.remove(node, extension);
	}

	@Override
	protected void saveExtension(final IExtension extension, final XMLElement element) {
		final ExternalResource previewUri = (ExternalResource) extension;
		final URI uri = previewUri.getUri();
		if (uri != null) {
			element.setAttribute("URI", uri.toString());
		}
		final float size = previewUri.getZoom();
		if (size != -1) {
			element.setAttribute("SIZE", Float.toString(size));
		}
		super.saveExtension(extension, element);
	}

	private JComponent createViewer(final MapModel map, final ExternalResource model) {
		final URI uri = model.getUri();
		if (uri == null) {
			return new JLabel("no file set");
		}
		final URI absoluteUri = model.getAbsoluteUri(map);
		if (absoluteUri == null) {
			return new JLabel(uri.toString());
		}
		final IViewerFactory factory = getViewerFactory(absoluteUri);
		if (factory == null) {
			return new JLabel(uri.toString());
		}
		JComponent viewer = null;
		try {
			final int maxWidth = ResourceController.getResourceController().getIntProperty("max_image_width");
			viewer = (JComponent) factory.createViewer(model, absoluteUri, maxWidth);
		}
		catch (final Exception e) {
			final String info = HtmlUtils.combineTextWithExceptionInfo(uri.toString(), e);
			return new JLabel(info);
		}
		if (viewer == null) {
			return new JLabel(uri.toString());
		}
		viewer.putClientProperty(IViewerFactory.class, factory);
		viewer.putClientProperty(ExternalResource.class, model);
		viewer.addMouseListener(mouseListener);
		viewer.addMouseMotionListener(mouseListener);
		return viewer;
	}

	private FileFilter getCombiFileFilter() {
		return new FactoryFileFilter(new CombiFactory());
	}

	public void addFactory(final IViewerFactory factory) {
		factories.add(factory);
	}

	public void removeFactory(final IViewerFactory factory) {
		factories.remove(factory);
	}

	/**
	 * This method attaches an image to a node, that is referenced with an uri
	 * @param uri : The image that is to be attached to a node
	 * @param node : The node that is worked upon
	 * @return : true if successful, false otherwise
	 */
	public boolean paste(final URI uri, final NodeModel node) {

		if (uri == null || getViewerFactory(uri) == null) {
			return false;
		}

		final ExternalResource preview = new ExternalResource(uri);
		undoableDeactivateHook(node);
		undoableActivateHook(node, preview);
		ProgressIcons.updateExtendedProgressIcons(node, uri.getPath());
		return true;
	}

	public static enum PasteMode{
		AS_SIBLING, AS_CHILD, INSIDE;
		public static PasteMode valueOf(boolean asSibling){
			return asSibling ? AS_SIBLING : AS_CHILD;
		}
	}

	public boolean paste(URI uri, final NodeModel node, final boolean isLeft) {
		return pasteImage(uri, node, PasteMode.INSIDE, isLeft);
	}

	public boolean paste(final File file, final NodeModel targetNode, final PasteMode mode, final boolean isLeft) {
		URI uri = uriOf(file);
		return pasteImage(uri, targetNode, mode, isLeft);
	}

	public boolean pasteImage(URI uri, final NodeModel targetNode, final PasteMode mode, final boolean isLeft) {
	    if (uri == null || getViewerFactory(uri) == null) {
			return false;
		}
		File file = new File(uri.getPath());
		boolean isFile = uri.getScheme().equals("file");
		if (isFile) {
	        if (!file.exists()) {
	        	return false;
	        }
	        final File mapFile = targetNode.getMap().getFile();
	        if (mapFile == null && LinkController.getLinkType() == LinkController.LINK_RELATIVE_TO_MINDMAP) {
	        	JOptionPane.showMessageDialog(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner(),
	        		TextUtils.getText("not_saved_for_image_error"), "Freeplane", JOptionPane.WARNING_MESSAGE);
	        	return false;
	        }
	        if (LinkController.getLinkType() != LinkController.LINK_ABSOLUTE) {
	        	uri = LinkController.toLinkTypeDependantURI(mapFile, file);
	        }
        }
		final MMapController mapController = (MMapController) Controller.getCurrentModeController().getMapController();
		final NodeModel node;
		if (mode.equals(PasteMode.INSIDE)) {
			node = targetNode;
		}
		else {
			node = mapController.newNode(file.getName(), targetNode.getMap());
			mapController.insertNode(node, targetNode, mode.equals(PasteMode.AS_SIBLING), isLeft, isLeft);
		}
		final ExternalResource preview = new ExternalResource(uri);
		undoableDeactivateHook(node);
		undoableActivateHook(node, preview);
		ProgressIcons.updateExtendedProgressIcons(node, file.getName());
		return true;
    }

	public IViewerFactory getCombiFactory() {
	    return new CombiFactory();
    }
}
