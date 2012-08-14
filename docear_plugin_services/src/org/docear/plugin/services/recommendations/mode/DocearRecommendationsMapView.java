package org.docear.plugin.services.recommendations.mode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.io.IOTools;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.communications.CommunicationsController;
import org.docear.plugin.services.recommendations.RecommendationEntry;
import org.docear.plugin.services.recommendations.dialog.RecommendationEntryComponent;
import org.docear.plugin.services.recommendations.mode.DocearRecommendationsNodeModel.RecommendationContainer;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;

public class DocearRecommendationsMapView extends MapView {

	private final class ListLayoutManager implements LayoutManager {
		public void removeLayoutComponent(Component comp) {				
		}

		public Dimension preferredLayoutSize(Container parent) {
			if(parent.getComponentCount() > 0) {
				Dimension compPref = parent.getComponent(0).getPreferredSize();
//				for(Component comp : parent.getComponents()) {
//					
//				}
				Insets insets = new Insets(0, 0, 0, 0); 
				if(parent instanceof JComponent) {
					insets = ((JComponent) parent).getInsets();
				}
				return new Dimension(compPref.width+insets.left+insets.right, compPref.height*parent.getComponentCount()+insets.top+insets.bottom);
			}
			return new Dimension();
		}

		public Dimension minimumLayoutSize(Container parent) {
			return parent.getMinimumSize();
		}

		public void layoutContainer(Container parent) {
			int i = parent.getComponentCount()-1;
			if(i < 0) {
				return;
			}
			Insets insets = new Insets(0, 0, parent.getHeight(), parent.getWidth()); 
			if(parent instanceof JComponent) {
				insets = ((JComponent) parent).getInsets();
			}
			int width = parent.getWidth()-insets.left-insets.right;
			int height = parent.getComponent(0).getPreferredSize().height;
			int x = insets.left;
			for(; i >= 0; i-- ) {
				Component comp = parent.getComponent(i);					
				int y = i*height + insets.top;										
				comp.setBounds(x, y, width, height);
			}
		}

		public void addLayoutComponent(String name, Component comp) {
							
		}
	}

	private static final long serialVersionUID = 1L;

	public DocearRecommendationsMapView(final MapModel model, final ModeController modeController) {
		super(model, modeController);
		this.setLayout(new BorderLayout());
		//this.removeAll();
		layoutModel(model);

	}

	private void layoutModel(MapModel model) {
		NodeModel node = model.getRootNode();
		layoutModel(node, null);

	}

	private void layoutModel(NodeModel node, Container parent) {
		if (node != null) {
			Object obj = node.getUserObject();
			Container container = parent;
			if (obj instanceof RecommendationEntry) {
				JComponent comp = getRecommendationComponent((RecommendationEntry) obj);
				container.add(comp);
			} else {
				if(obj instanceof RecommendationContainer) {
					container = getNewRecommandationContainerComponent(obj.toString());
					this.add(container);
				}
				else {
					if(container == null) {
						container = getNewEmptyContainerComponent();
						this.add(container);
					}
					if(obj instanceof Component) {
						container.add((Component) obj);
					}
					else {
						container.add(new JLabel(obj.toString()));
					}					
				}
				

			}
			if (node.hasChildren()) {
				for (NodeModel child : node.getChildren()) {
					layoutModel(child, container);
				}
			}
		}
	}

	private JComponent getRecommendationComponent(final RecommendationEntry recommendation) {
		RecommendationEntryComponent comp = new RecommendationEntryComponent(recommendation);
		comp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				URL page = recommendation.getClickUrl();
				try {
					page = redirectRecommendationLink(recommendation, page);
				} 
				catch (Exception ex) {
					//click didn't work
					LogUtils.info(ex.getMessage());
				}
				
				if(e.getID() == RecommendationEntryComponent.OPEN_RECOMMENDATION) {					
					try {
						Controller.getCurrentController().getViewController().openDocument(page);
					} 
					catch (Exception ex) {
						LogUtils.warn("could not open link to (" + recommendation.getLink() + ")", ex);
					}
				}
				else if(e.getID() == RecommendationEntryComponent.IMPORT_RECOMMENDATION) {
					DocearController.getController().dispatchDocearEvent(new DocearEvent(page, DocearEventType.IMPORT_TO_LIBRARY, recommendation.getTitle()));
				}
			}

			private URL redirectRecommendationLink(final RecommendationEntry recommendation, URL page) throws IOException, MalformedURLException {
				URLConnection connection;
				connection = recommendation.getClickUrl().openConnection();
				if(connection instanceof HttpURLConnection) {
					HttpURLConnection hconn = (HttpURLConnection) connection;							
				    hconn.setInstanceFollowRedirects(false);
				    String accessToken = CommunicationsController.getController().getAccessToken();
				    hconn.addRequestProperty("accessToken", accessToken);
				    
				    int response = hconn.getResponseCode();
				    boolean redirect = (response >= 300 && response <= 399);
				    

				    /*
				     * In the case of a redirect, we want to actually change the URL
				     * that was input to the new, redirected URL
				     */
				    if (redirect) {
						String loc = connection.getHeaderField("Location");
						if (loc.startsWith("http", 0)) {
						    page = new URL(loc);
						} else {
						    page = new URL(page, loc);
						}
					} 
				    else {
				    	if(response == 200) {
					    	String content = IOTools.getStringFromStream(connection.getInputStream(), "UTF-8");
					    	String searchPattern = "<meta http-equiv=\"REFRESH\" content=\"0;url=";
							int pos = content.indexOf(searchPattern);
					    	if(pos >= 0) {
					    		String loc = content.substring(pos+searchPattern.length());
					    		loc = loc.substring(0,loc.indexOf("\""));
					    		if (loc.startsWith("http", 0)) {
								    page = new URL(loc);
								} else {
								    page = new URL(page, loc);
								}
					    	}
					    }
				    }
				}
				return page;
			}
		});
		return comp;
	}

	private Container getNewRecommandationContainerComponent(String title) {
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		panel.setBorder(new TitledBorder(title));
		panel.setLayout(new ListLayoutManager());
		this.add(getNewButtonBar(), BorderLayout.NORTH);
		this.add(panel, BorderLayout.CENTER);
		return panel;
	}
	
	private Component getNewButtonBar() {
		JPanel panel = new JPanel(new LayoutManager() {
			
			public void removeLayoutComponent(Component comp) {				
			}
			
			public Dimension preferredLayoutSize(Container parent) {
				int count = parent.getComponentCount();
				for(Component comp : parent.getComponents()) {
					Dimension comDim = comp.getPreferredSize();
					if(comDim != null) {
						return new Dimension((parent.getInsets().left+parent.getInsets().right+comDim.width)*count, parent.getInsets().top+parent.getInsets().bottom+comDim.height);
					}
				}
				return null;
			}
			
			public Dimension minimumLayoutSize(Container parent) {
				for(Component comp : parent.getComponents()) {
					int count = parent.getComponentCount();
					Dimension comDim = comp.getMinimumSize();
					if(comDim != null) {
						return new Dimension((parent.getInsets().left+parent.getInsets().right+comDim.width)*count, parent.getInsets().top+parent.getInsets().bottom+comDim.height);
					}
				}
				return null;
			}
			
			public void layoutContainer(Container parent) {
				int right = parent.getWidth()-parent.getInsets().right;
				int top = parent.getInsets().top;
				int count = parent.getComponentCount();
				for(Component comp : parent.getComponents()) {
					int x = right-comp.getWidth()*count;
					comp.setLocation(x,top);
					comp.setSize(comp.getPreferredSize());
					count--;
				}
			}
			
			public void addLayoutComponent(String name, Component comp) {
				
			}
		});
		panel.setBackground(Color.white);
		panel.setBorder(new EmptyBorder(5, 5, 0, 5));
		JButton refreshButton = new JButton(new ImageIcon(RecommendationEntryComponent.class.getResource("/icons/view-refresh-7_32x32.png")));
		refreshButton.setMinimumSize(new Dimension(50, 50));
		refreshButton.setPreferredSize(new Dimension(50, 50));
		refreshButton.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				ServiceController.getController().getRecommenationMode().getMapController().refreshRecommendations();
			}
		});
		refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		refreshButton.setToolTipText(TextUtils.getText("recommendations.refresh.title"));
		panel.add(refreshButton);
		JButton closeButton = new JButton(new ImageIcon(RecommendationEntryComponent.class.getResource("/icons/window-close-2_32x32.png")));
		closeButton.setMinimumSize(new Dimension(50, 50));
		closeButton.setPreferredSize(new Dimension(50, 50));
		closeButton.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new  Runnable() {
					public void run() {
						Controller.getCurrentController().close(true);	
					}
				});
			}
		});
		closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		closeButton.setToolTipText(TextUtils.getText("recommendations.close.title"));
		panel.add(closeButton);
		return panel;
	}

	private Container getNewEmptyContainerComponent() {
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		panel.setLayout(new ListLayoutManager());
		this.add(getNewButtonBar(), BorderLayout.NORTH);
		this.add(panel, BorderLayout.CENTER);
		//this.add(panel);
		return panel;
	}

	public void paint(Graphics g) {
		try {
			super.paintInternal(g);
		}
		catch (Exception ex) {
			//DOCEAR - maybe reset to mind map mode
			//Controller.getCurrentController().selectMode(MModeController.MODENAME);
		}
	}
	
	public void paintChildren(Graphics g) {
		super.paintChildrenInternal(g);
	}

	public Component add(Component comp, int index) {
		return super.add(comp, index);
	}
	
	public void centerNode(final NodeView node, boolean slowScroll) {
		node.setLocation(-9999, -9999);
	}
}
