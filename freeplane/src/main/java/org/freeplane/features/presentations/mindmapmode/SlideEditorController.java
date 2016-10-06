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
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

class SlideEditorController{
	
	private SlideModel slide;
	
	private final JToggleButton tglbtnHighlightVisibleNodes;
	private final JButton btnSetSelectedNode;
	private final JToggleButton tglbtnChangeZoom;
	private final JLabel lblZoomFactor;
	private final JCheckBox checkBoxShowOnlySpecificNodes;
	private final JCheckBox checkBoxShowAncestors;
	private final JCheckBox checkBoxShowDescendants;
	private final JButton btnAddNodes;
	private final JButton btnRemoveNodes;
	private final JToggleButton tglbtnSetFilter;
	
	private final JComponent[] allButtons;
	private final JComponent[] specificNodeButtons;
	private final JComponent[] filterRelatedButtons;

	private final SlideChangeListener slideChangeListener;
	
	public SlideEditorController() {
		tglbtnHighlightVisibleNodes = createHighlightSlideContentToggleButton();
		btnSetSelectedNode = createSetSelectedNodeButton();
		tglbtnChangeZoom = createSetZoomToggleButton();
		lblZoomFactor = new JLabel("100 %");
		checkBoxShowOnlySpecificNodes = createOnlySpecificNodesCheckBox();
		checkBoxShowAncestors = createShowAncestorsCheckBox();
		checkBoxShowDescendants = createShowDescendantsCheckBox();
		btnAddNodes = createAddNodesButton();
		btnRemoveNodes = createRemoveNodesButton();
		tglbtnSetFilter = createSetFilterToggleButton();
		
		allButtons = new JComponent[]{tglbtnHighlightVisibleNodes, btnSetSelectedNode, tglbtnChangeZoom, lblZoomFactor, 
				checkBoxShowOnlySpecificNodes, checkBoxShowAncestors, checkBoxShowDescendants, btnAddNodes, btnRemoveNodes, tglbtnSetFilter};
		specificNodeButtons = new JComponent[]{btnAddNodes, btnRemoveNodes};
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
		JButton btnSetSelectedNode = new JButton("Set selection");
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
	
	private JCheckBox createOnlySpecificNodesCheckBox() {
		final JCheckBox checkBoxOnlySpecificNodes = new JCheckBox("Show only specific nodes");
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


	private JButton createRemoveNodesButton() {
		final JButton btnRemoveNodes = new JButton("Remove nodes");
		btnRemoveNodes.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnRemoveNodes.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<String> selection = Controller.getCurrentController().getSelection().getOrderedSelectionIds();
				slide.removeVisibleNodeIds(selection);
			}
		});
		return btnRemoveNodes;
	}


	private JButton createAddNodesButton() {
		final JButton btnAddNodes = new JButton("Add nodes");
		btnAddNodes.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnAddNodes.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<String> selection = Controller.getCurrentController().getSelection().getOrderedSelectionIds();
				slide.addVisibleNodeIds(selection);
			}
		});
		return btnAddNodes;
	}

	private JToggleButton createHighlightSlideContentToggleButton() {
		JToggleButton tglbtnHighlightSlideContent = new JToggleButton("Highlight content");
		tglbtnHighlightSlideContent.setToolTipText("Highlight visible and selected nodes in the map");
		tglbtnHighlightSlideContent.setAlignmentX(Component.CENTER_ALIGNMENT);
		tglbtnHighlightSlideContent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				slide.setHighlightsVisibleNodes(! slide.highlightsVisibleNodes());
			}
		});
		return tglbtnHighlightSlideContent;
	}


	Box createSlideContentBox() {
		
		Box content = Box.createVerticalBox();
		content.setBorder(new TitledBorder(null, "Slide content", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		Box slideButtons = Box.createVerticalBox();
		content.add(slideButtons);
		slideButtons.add(tglbtnHighlightVisibleNodes);
		slideButtons.add(btnSetSelectedNode);
		Box zoomBox = Box.createHorizontalBox();
		zoomBox.add(tglbtnChangeZoom);
		zoomBox.add(lblZoomFactor);
		slideButtons.add(zoomBox);
		slideButtons.add(checkBoxShowOnlySpecificNodes);
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.add(btnAddNodes);
		horizontalBox.add(btnRemoveNodes);
		slideButtons.add(horizontalBox);
		slideButtons.add(checkBoxShowAncestors);
		slideButtons.add(checkBoxShowDescendants);
		slideButtons.add(tglbtnSetFilter);

		return content;
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
		checkBoxShowOnlySpecificNodes.setSelected(showsOnlySpecificNodes);
		for(JComponent c : specificNodeButtons)
			c.setEnabled(showsOnlySpecificNodes);
		for(JComponent c : filterRelatedButtons)
			c.setEnabled(showsOnlySpecificNodes || slide.getFilterCondition() != null);
		tglbtnHighlightVisibleNodes.setSelected(slide.highlightsVisibleNodes());;
		final boolean changesZoom = slide.changesZoom();
		tglbtnChangeZoom.setSelected(changesZoom);
		lblZoomFactor.setText(changesZoom ? Math.round(slide.getZoom() * 100) + "%" : "");
		checkBoxShowOnlySpecificNodes.setSelected(showsOnlySpecificNodes);
		checkBoxShowAncestors.setSelected(slide.showsAncestors());
		checkBoxShowDescendants.setSelected(slide.showsDescendants());
		checkBoxShowAncestors.setSelected(slide.showsAncestors());
		tglbtnSetFilter.setSelected(slide.getFilterCondition() != null);
	}
	
	

}