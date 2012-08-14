package org.docear.plugin.pdfutilities.listener;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.features.AnnotationModel;
import org.docear.plugin.core.features.IAnnotation;
import org.docear.plugin.core.logger.DocearLogEvent;
import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.actions.UpdateMonitoringFolderAction;
import org.docear.plugin.pdfutilities.util.MonitoringUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.ui.components.MultipleImage;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.ZoomableLabelUI;

public class DocearNodeMouseMotionListener implements IMouseListener {

	private IMouseListener mouseListener;	
	private boolean wasFocused;
	private final Timer showTimer;
	private final Timer hideTimer;

	public DocearNodeMouseMotionListener(IMouseListener mouseListener) {
		this.mouseListener = mouseListener;
		showTimer = new Timer(750, null);
		showTimer.setRepeats(false);
		hideTimer = new Timer(150, null);
		hideTimer.setRepeats(false);
	}	

	public void mouseDragged(MouseEvent e) {
		this.mouseListener.mouseDragged(e);
	}

	public void mouseMoved(MouseEvent e) {
		if(e.getSource() instanceof MainView) {
			final MainView view = (MainView) e.getSource();
			Rectangle bounds = ((ZoomableLabelUI)view.getUI()).getIconR(view);
			Point p = e.getPoint();
			if(bounds.contains(p)) {
				if(view.getIcon() instanceof MultipleImage) {
					Rectangle iconR = ((MultipleImage)view.getIcon()).getIconR(PdfUtilitiesController.REFRESH_MONITORING_ICON);
					if(iconR != null) {
						float zoom = Controller.getCurrentController().getViewController().getZoom();
						iconR.setLocation((int) (iconR.x*zoom), iconR.y);
						iconR.setSize((int)(iconR.width*zoom), (int)(iconR.height*zoom));
						iconR.translate(bounds.x, bounds.y);
						if(iconR.contains(p)) {
							if(!showTimer.isRunning() && !hideTimer.isRunning()) {
								resetTimer();							
								showTimer.addActionListener(new ShowToolTipAction(view));
								showTimer.start();
							}
							return;
						}
					}
					else {
						resetTimer();
					}
				}
				else {
					resetTimer();
				}
			}
		}
		
		this.mouseListener.mouseMoved(e);
	}

	private List<NodeModel> getMonitorNodes(NodeModel node) {
		List<NodeModel> result = new ArrayList<NodeModel>();
		if(MonitoringUtils.isMonitoringNode(node)){
			result.add(node);
		}
		for(NodeModel child : node.getChildren()){
			result.addAll(getMonitorNodes(child));
		}
		return result;
	}
	
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1 && e.getSource() instanceof MainView) {
			MainView view = (MainView) e.getSource();
			Rectangle bounds = ((ZoomableLabelUI)view.getUI()).getIconR(view);
			Point p = e.getPoint();
			if(bounds.contains(p)) {
				if(view.getIcon() instanceof MultipleImage) {
					Rectangle iconR = ((MultipleImage)view.getIcon()).getIconR(PdfUtilitiesController.REFRESH_MONITORING_ICON);
					if(iconR != null) {
						float zoom = Controller.getCurrentController().getViewController().getZoom();
						iconR.setLocation((int) (iconR.x*zoom), iconR.y);
						iconR.setSize((int)(iconR.width*zoom), (int)(iconR.height*zoom));
						iconR.translate(bounds.x, bounds.y);
						if(iconR.contains(p)) {												
							UpdateMonitoringFolderAction.updateNodesAgainstMonitoringDir(getMonitorNodes(Controller.getCurrentController().getViewController().getMap().getRootNode()), false);
							return;
						}
					}
				}
			}
		}
		boolean openOnPage = ResourceController.getResourceController().getBooleanProperty(
				PdfUtilitiesController.OPEN_PDF_VIEWER_ON_PAGE_KEY);		
		
		if (!openOnPage) {
			this.mouseListener.mouseClicked(e);
			return;
		}

		if (/*wasFocused() && */(e.getModifiers() & ~(InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK)) == InputEvent.BUTTON1_MASK) {
			final MainView component = (MainView) e.getComponent();
			final ModeController modeController = Controller.getCurrentModeController();
			NodeModel node = null;
			try {
				node = ((MainView) e.getSource()).getNodeView().getModel();
			}
			catch (Exception ex) {			
			}
			
			if (node==null) {
				node = modeController.getMapController().getSelectedNode();
			}
			
			if (component.isInFollowLinkRegion(e.getX())) {
				writeToLog(node);
			}
			if (!component.isInFollowLinkRegion(e.getX()) || !MonitoringUtils.isPdfLinkedNode(node)) {				
				this.mouseListener.mouseClicked(e);
				return;
			}
			
			URI uri = Tools.getAbsoluteUri(node);
			if(uri == null) { 
				this.mouseListener.mouseClicked(e);
				return;
			}
			
			IAnnotation annotation = null;
			try {
				annotation = node.getExtension(AnnotationModel.class);
			}
			catch(Exception ex) {				
			}
			
			LinkController.getController().onDeselect(node);
			if (!PdfUtilitiesController.getController().openPdfOnPage(uri, annotation)) {
				this.mouseListener.mouseClicked(e);
				return;
			}
			LinkController.getController().onSelect(node);
			
			
		}
		else {
			this.mouseListener.mouseClicked(e);
		}
	}
	
	

	private void writeToLog(NodeModel node) {
		URI uri = Tools.getAbsoluteUri(node);
		if(uri == null) {
			return;
		}
		if ("file".equals(uri.getScheme())) {
			File f = WorkspaceUtils.resolveURI(uri);
			//if map file is opened, then there is a MapLifeCycleListener Event
			if (f != null && !f.getName().endsWith(".mm")) {
				DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.FILE_OPENED,  f);
			}
		}
		else {					
			try {
				DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.OPEN_URL, uri.toURL());
			} catch (MalformedURLException ex) {						
				LogUtils.warn(ex);
			}
		}
	}

	

	public void mousePressed(MouseEvent e) {
		final MainView component = (MainView) e.getComponent();
		wasFocused = component.hasFocus();
		this.mouseListener.mousePressed(e);
	}

	public void mouseReleased(MouseEvent e) {
		this.mouseListener.mouseReleased(e);
	}

	public void mouseEntered(MouseEvent e) {		
		this.mouseListener.mouseEntered(e);
	}

	public void mouseExited(MouseEvent e) {
		resetTimer();
		this.mouseListener.mouseExited(e);
	}

	private void resetTimer() {
		showTimer.stop();
		for(ActionListener l : showTimer.getActionListeners()) {
			showTimer.removeActionListener(l);
		}
	}

	public boolean wasFocused() {
		return wasFocused;
	}
	
	private class ShowToolTipAction implements ActionListener {
		private final JComponent comp;
		
		public ShowToolTipAction(JComponent c) {
			this.comp = c;
		}
		
		public void actionPerformed(ActionEvent e) {
			PopupFactory popupFactory = PopupFactory.getSharedInstance();
			JToolTip tip = new JToolTip();
			tip.setTipText(TextUtils.getText("docear.monitoring.reload.name"));
			final Point locationOnScreen = comp.getLocationOnScreen();
			final int height = comp.getHeight();
			Rectangle sBounds = comp.getGraphicsConfiguration().getBounds();
			final int minX = sBounds.x;
			final int maxX = sBounds.x + sBounds.width;
			final int minY = sBounds.y;
			final int maxY = sBounds.y + sBounds.height;
			int x = locationOnScreen.x;
			int y = locationOnScreen.y + height;
			final Dimension tipSize = tip.getPreferredSize();
			final int tipWidth = tipSize.width;
			if(x + tipWidth > maxX){
				x = maxX - tipWidth;
			}
			if(x < minX){
				x = minX;
			}
			final int tipHeight = tipSize.height;
			if(y + tipHeight > maxY){
				if(locationOnScreen.y - tipHeight > minY){
					y = locationOnScreen.y - tipHeight;
				}
				else{
					y = maxY - tipHeight;
				}
			}
			if(y < minY){
				y = minY;
			}
			final Popup tipPopup = popupFactory.getPopup(comp, tip, x, y);
			tipPopup.show();
			showTimer.removeActionListener(this);
			hideTimer.addActionListener(new HideToolTipAction(tipPopup, tip, comp));
			hideTimer.start();
		}
		
	}
	
	private class HideToolTipAction implements ActionListener {
		private final JToolTip tip;
		private final Popup popup;
		private final JComponent comp;
		
		public HideToolTipAction(Popup popup, JToolTip tip, JComponent component) {
			this.popup = popup;
			this.tip = tip;
			this.comp = component;
		}
		
		public void actionPerformed(ActionEvent e) {
			final KeyboardFocusManager currentKeyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            final Window activeWindow = currentKeyboardFocusManager.getActiveWindow();
			if(activeWindow instanceof JDialog && ((JDialog) activeWindow).isModal() 
            		&& ! SwingUtilities.isDescendingFrom(Controller.getCurrentController().getViewController().getMapView(), activeWindow)){
				popup.hide();
				hideTimer.removeActionListener(this);
				hideTimer.stop();
            } 
			
                    
			if(tip.getMousePosition(true) != null || mouseOverComponent()){
				hideTimer.restart();
				return;
			}
	        final Component focusOwner = currentKeyboardFocusManager.getFocusOwner();
			if(focusOwner != null){
				if(SwingUtilities.isDescendingFrom(focusOwner, tip)){
					hideTimer.restart();
					return;
				}
			}
			
			
			popup.hide();
			hideTimer.removeActionListener(this);
			hideTimer.stop();
			
		}
		
		protected boolean mouseOverComponent() {
			if(comp.isShowing()){				
				if(comp instanceof MainView) {
					final MainView view = (MainView) comp;
					Rectangle bounds = ((ZoomableLabelUI)view.getUI()).getIconR(view);
					final Point mousePosition = comp.getMousePosition(true);
					if(mousePosition == null) {
						return false;
					}
					if(bounds.contains(mousePosition)) {
						if(view.getIcon() instanceof MultipleImage) {
							Rectangle iconR = ((MultipleImage)view.getIcon()).getIconR(PdfUtilitiesController.REFRESH_MONITORING_ICON);
							if(iconR != null) {
								float zoom = Controller.getCurrentController().getViewController().getZoom();
								iconR.setLocation((int) (iconR.x*zoom), iconR.y);
								iconR.setSize((int)(iconR.width*zoom), (int)(iconR.height*zoom));
								iconR.translate(bounds.x, bounds.y);
								if(iconR.contains(mousePosition)) {
									return true;
								}
							}
						}
					}
				}
			}
			return false;
		}
		
	}

}
