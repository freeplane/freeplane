package org.docear.plugin.services.recommendations.mode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.services.recommendations.RecommendationEntry;
import org.docear.plugin.services.recommendations.dialog.RecommendationEntryComponent;
import org.docear.plugin.services.recommendations.mode.DocearRecommendationsNodeModel.RecommendationContainer;
import org.freeplane.core.util.LogUtils;
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
				if(e.getID() == RecommendationEntryComponent.OPEN_RECOMMENDATION) {
					try {
						Controller.getCurrentController().getViewController().openDocument(recommendation.getLink());
					} catch (Exception ex) {
						LogUtils.warn("could not open link to (" + recommendation.getLink() + ")", ex);
					}
				}
				else if(e.getID() == RecommendationEntryComponent.IMPORT_RECOMMENDATION) {
					DocearController.getController().dispatchDocearEvent(new DocearEvent(recommendation.getLink(), "IMPORT_TO_LIBRARY"));
				}
			}
		});
		return comp;
	}

	private Container getNewRecommandationContainerComponent(String title) {
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		panel.setBorder(new TitledBorder(title));
		panel.setLayout(new ListLayoutManager());
		this.add(panel);
		return panel;
	}
	
	private Container getNewEmptyContainerComponent() {
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		panel.setLayout(new ListLayoutManager());
		this.add(panel);
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
