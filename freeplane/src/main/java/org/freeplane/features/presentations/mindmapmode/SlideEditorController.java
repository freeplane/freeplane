package org.freeplane.features.presentations.mindmapmode;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import org.freeplane.features.filter.FilterComposerDialog;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

class SlideEditorController{
	
	private SlideModel slide;
	
	private final JButton btnHighlightSelectedNodes;
	private final JButton btnSetSelectedNodes;
	private final JCheckBox checkBoxCentersSelectedNode;
	private final JToggleButton tglbtnChangeZoom;
	private final JLabel lblZoomFactor;
	private final JCheckBox checkBoxShowOnlySelectedNodes;
	private final JCheckBox checkBoxShowAncestors;
	private final JCheckBox checkBoxShowDescendants;
	private final JToggleButton tglbtnSetFilter;
	
	private final JComponent[] allButtons;
	private final JComponent[] filterRelatedButtons;

	private final SlideChangeListener slideChangeListener;

	
	public SlideEditorController() {
		btnSetSelectedNodes = createSetSelectedNodeButton();
		btnHighlightSelectedNodes = createHighlightSelectedNodesButton();
		checkBoxCentersSelectedNode = createCentersSelectedNodeCheckBox();
		tglbtnChangeZoom = createSetZoomToggleButton();
		lblZoomFactor = new JLabel("100 %");
		checkBoxShowOnlySelectedNodes = createOnlySelectedNodesCheckBox();
		checkBoxShowAncestors = createShowAncestorsCheckBox();
		checkBoxShowDescendants = createShowDescendantsCheckBox();
		tglbtnSetFilter = createSetFilterToggleButton();
		
		allButtons = new JComponent[] { btnHighlightSelectedNodes, btnSetSelectedNodes, checkBoxCentersSelectedNode,
		        tglbtnChangeZoom, lblZoomFactor, 
		        checkBoxShowOnlySelectedNodes, checkBoxShowAncestors, checkBoxShowDescendants, tglbtnSetFilter };
		filterRelatedButtons = new JComponent[]{checkBoxShowAncestors, checkBoxShowDescendants};
		slideChangeListener = new SlideChangeListener() {
			
			@Override
			public void onSlideModelChange(SlideChangeEvent changeEvent) {
				updateUI();
			}
		};
		
		disableUI();
	}

	private JButton createSetSelectedNodeButton() {
		JButton btnSetSelectedNode = new JButton("Set");
		btnSetSelectedNode.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnSetSelectedNode.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<String> selection = Controller.getCurrentController().getSelection().getOrderedSelectionIds();
				slide.setSelectedNodeIds(new LinkedHashSet<>(selection));
			}
		});

		return btnSetSelectedNode;
	}

	private JToggleButton createSetZoomToggleButton() {
		final JToggleButton btnSetsZoom = new JToggleButton("Set zoom");
		btnSetsZoom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final boolean changesZoom = ! slide.changesZoom();
				slide.setChangesZoom(changesZoom);
				if(changesZoom) {
					slide.setZoom(Controller.getCurrentController().getMapViewManager().getZoom());
				}
			}
		});
		return btnSetsZoom;
	}
	private JCheckBox createCentersSelectedNodeCheckBox() {
		final JCheckBox checkBoxOnlySpecificNodes = new JCheckBox("Center selected node");
		checkBoxOnlySpecificNodes.setAlignmentX(Component.CENTER_ALIGNMENT);
		checkBoxOnlySpecificNodes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				slide.setCentersSelectedNode(!slide.centersSelectedNode());
			}
		});
		return checkBoxOnlySpecificNodes;
	}

	private JCheckBox createOnlySelectedNodesCheckBox() {
		final JCheckBox checkBoxOnlySpecificNodes = new JCheckBox("Show only selected nodes");
		checkBoxOnlySpecificNodes.setAlignmentX(Component.CENTER_ALIGNMENT);
		checkBoxOnlySpecificNodes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				slide.setShowsOnlySpecificNodes(! slide.showsOnlySpecificNodes());
			}
		});
		return checkBoxOnlySpecificNodes;
	}

	private JCheckBox createShowAncestorsCheckBox() {
		final JCheckBox checkBoxShowAncestors = new JCheckBox("Show ancestors");
		checkBoxShowAncestors.setAlignmentX(Component.CENTER_ALIGNMENT);
		checkBoxShowAncestors.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				slide.setShowsAncestors(! slide.showsAncestors());
			}
		});
		return checkBoxShowAncestors;
	}

	private JCheckBox createShowDescendantsCheckBox() {
		final JCheckBox checkBoxShowDescendants = new JCheckBox("Show descendants");
		checkBoxShowDescendants.setAlignmentX(Component.CENTER_ALIGNMENT);
		checkBoxShowDescendants.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				slide.setShowsDescendants(! slide.showsDescendants());
			}
		});
		return checkBoxShowDescendants;
	}


	private JToggleButton createSetFilterToggleButton() {
		JToggleButton tglbtnSetFilter = new JToggleButton("Set filter");
		tglbtnSetFilter.setAlignmentX(Component.CENTER_ALIGNMENT);
		tglbtnSetFilter.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
			    final FilterComposerDialog filterComposerDialog = FilterController.getCurrentFilterController().getFilterComposerDialog();
			    filterComposerDialog.acceptMultipleConditions(true);
			    ASelectableCondition oldCondition = slide.getFilterCondition();
				if(oldCondition != null)
			    	filterComposerDialog.addCondition(oldCondition);
			    filterComposerDialog.show();
			    List<ASelectableCondition> conditions = filterComposerDialog.getConditions();
			    if(filterComposerDialog.isSuccess()) {
					ASelectableCondition newCondition = conditions.isEmpty() ? null : conditions.get(0);
					slide.setFilterCondition(newCondition);
				}
			    
			}
		});
		return tglbtnSetFilter;
	}

	private JButton createHighlightSelectedNodesButton() {
		JButton btnHighlightSlideContent = new JButton("Select");
		btnHighlightSlideContent.setToolTipText("Highlight visible and selected nodes in the map");
		btnHighlightSlideContent.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnHighlightSlideContent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Set<String> selectedNodeIds = slide.getSelectedNodeIds();
				MapModel map = Controller.getCurrentController().getMap();
				ArrayList<NodeModel> selectedNodes = new ArrayList<>(selectedNodeIds.size());
				for (String id : selectedNodeIds) {
					NodeModel node = map.getNodeForID(id);
					if (node != null && node.isVisible())
						selectedNodes.add(node);
				}
				if (!selectedNodes.isEmpty()) {
					NodeModel[] nodes = selectedNodeIds.toArray(new NodeModel[] {});
					Controller.getCurrentController().getSelection().replaceSelection(nodes);
				}
			}
		});
		return btnHighlightSlideContent;
	}


	Box createSlideContentBox() {
		
		Box content = Box.createVerticalBox();
		
		Box selectionBox = Box.createHorizontalBox();
		selectionBox.add(btnSetSelectedNodes);
		selectionBox.add(btnHighlightSelectedNodes);
		content.add(selectionBox);
		content.add(checkBoxCentersSelectedNode);
		Box zoomBox = Box.createHorizontalBox();
		zoomBox.add(tglbtnChangeZoom);
		zoomBox.add(lblZoomFactor);
		content.add(zoomBox);
		content.add(checkBoxShowOnlySelectedNodes);
		Box specificNodeButtons = Box.createHorizontalBox();
		content.add(specificNodeButtons);
		content.add(checkBoxShowAncestors);
		content.add(checkBoxShowDescendants);
		content.add(tglbtnSetFilter);

		Box contentWithMargins = Box.createHorizontalBox();
		contentWithMargins
		    .setBorder(new TitledBorder(null, "Slide content", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentWithMargins.add(Box.createHorizontalGlue());
		contentWithMargins.add(content);
		contentWithMargins.add(Box.createHorizontalGlue());
		return contentWithMargins;
	}


	public void setSlide(SlideModel newSlide) {
		if(slide != null)
			slide.removeSelectionChangeListener(slideChangeListener);
		this.slide = newSlide;
		if(newSlide != null){
			for(JComponent c : allButtons)
				c.setEnabled(true);
			updateUI();
			slide.addSelectionChangeListener(slideChangeListener);
		}
		else{
			disableUI();
		}
	}


	private void disableUI() {
		for(JComponent c : allButtons)
			c.setEnabled(false);
	}


	private void updateUI() {
		final boolean showsOnlySpecificNodes = slide.showsOnlySpecificNodes();
		checkBoxShowOnlySelectedNodes.setSelected(showsOnlySpecificNodes);
		final boolean centersSelectedNode = slide.centersSelectedNode();
		checkBoxCentersSelectedNode.setSelected(centersSelectedNode);
		for(JComponent c : filterRelatedButtons)
			c.setEnabled(showsOnlySpecificNodes || slide.getFilterCondition() != null);
		checkBoxCentersSelectedNode.setEnabled(true);
		final boolean changesZoom = slide.changesZoom();
		tglbtnChangeZoom.setSelected(changesZoom);
		lblZoomFactor.setText(changesZoom ? Math.round(slide.getZoom() * 100) + "%" : "");
		checkBoxShowOnlySelectedNodes.setSelected(showsOnlySpecificNodes);
		checkBoxShowAncestors.setSelected(slide.showsAncestors());
		checkBoxShowDescendants.setSelected(slide.showsDescendants());
		checkBoxShowAncestors.setSelected(slide.showsAncestors());
		tglbtnSetFilter.setSelected(slide.getFilterCondition() != null);
	}
	
	

}