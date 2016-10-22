package org.freeplane.features.presentations.mindmapmode;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import org.freeplane.core.ui.textchanger.TranslatedElementFactory;
import org.freeplane.features.filter.FilterComposerDialog;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.mode.Controller;

class SlideEditorController{
	
	private Slide slide;
	
	private final JButton btnSelectNodes;
	private final JButton btnSetSelectedNodes;
	private final JButton btnAddSelectedNodes;
	private final JButton btnRemoveSelectedNodes;
	private final JCheckBox checkBoxCentersSelectedNode;
	private final JToggleButton tglbtnChangeZoom;
	private final JLabel lblZoomFactor;
	private final JCheckBox checkBoxShowOnlySelectedNodes;
	private final JCheckBox checkBoxShowAncestors;
	private final JCheckBox checkBoxShowDescendants;
	private final JToggleButton tglbtnSetFilter;
	private final JComponent filterConditionComponentBox;
	
	private final JComponent[] allButtons;
	private final JComponent[] filterRelatedButtons;

	private final SlideChangeListener slideChangeListener;

	
	public SlideEditorController() {
		btnSetSelectedNodes = createSetSelectedNodeButton();
		btnAddSelectedNodes = createAddSelectedNodeButton();
		btnRemoveSelectedNodes = createRemoveSelectedNodeButton();
		btnSelectNodes = createSelectNodesButton();
		checkBoxCentersSelectedNode = createCentersSelectedNodeCheckBox();
		tglbtnChangeZoom = createSetZoomToggleButton();
		lblZoomFactor = new JLabel("100 %");
		lblZoomFactor.setPreferredSize(lblZoomFactor.getPreferredSize());
		checkBoxShowOnlySelectedNodes = createOnlySelectedNodesCheckBox();
		checkBoxShowAncestors = createShowAncestorsCheckBox();
		checkBoxShowDescendants = createShowDescendantsCheckBox();
		tglbtnSetFilter = createSetFilterToggleButton();
		filterConditionComponentBox = Box.createHorizontalBox();
		
		allButtons = new JComponent[] { btnSelectNodes, btnSetSelectedNodes, btnAddSelectedNodes,
		        btnRemoveSelectedNodes, checkBoxCentersSelectedNode,
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
		JButton btnSetSelectedNode = TranslatedElementFactory.createButton("slide.set");
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

	private JButton createAddSelectedNodeButton() {
		JButton btnSetSelectedNode = TranslatedElementFactory.createButton("slide.add");
		btnSetSelectedNode.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnSetSelectedNode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<String> selection = Controller.getCurrentController().getSelection()
				    .getOrderedSelectionIds();
				slide.addSelectedNodeIds(selection);
			}
		});
		return btnSetSelectedNode;
	}

	private JButton createRemoveSelectedNodeButton() {
		JButton btnSetSelectedNode = TranslatedElementFactory.createButton("slide.remove");
		btnSetSelectedNode.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnSetSelectedNode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<String> selection = Controller.getCurrentController().getSelection()
				    .getOrderedSelectionIds();
				slide.removeSelectedNodeIds(selection);
			}
		});
		return btnSetSelectedNode;
	}

	private JToggleButton createSetZoomToggleButton() {
		final JToggleButton btnSetsZoom = TranslatedElementFactory.createToggleButton("slide.setzoom");
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
		final JCheckBox checkBoxOnlySpecificNodes = TranslatedElementFactory.createCheckBox("slide.centernode");
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
		final JCheckBox checkBoxOnlySpecificNodes = TranslatedElementFactory.createCheckBox("slide.showonlyselected");
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
				
		final JCheckBox checkBoxShowAncestors = TranslatedElementFactory.createCheckBox("slide.showancestors");
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
		final JCheckBox checkBoxShowDescendants = TranslatedElementFactory.createCheckBox("slide.descendants");
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
		JToggleButton tglbtnSetFilter = TranslatedElementFactory.createToggleButton("slide.setfilter");
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

	private JButton createSelectNodesButton() {
		JButton btnHighlightSlideContent = TranslatedElementFactory.createButton("slide.select");
		TranslatedElementFactory.createTooltip(btnHighlightSlideContent, "slide.select.tooltip");
		btnHighlightSlideContent.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnHighlightSlideContent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				slide.replaceCurrentSelection();
			}
		});
		return btnHighlightSlideContent;
	}


	Box createSlideContentBox() {
		
		Box content = Box.createVerticalBox();
		
		Box selectionBox = Box.createHorizontalBox();
		selectionBox.add(btnSetSelectedNodes);
		selectionBox.add(btnAddSelectedNodes);
		selectionBox.add(btnRemoveSelectedNodes);
		selectionBox.add(btnSelectNodes);
		content.add(selectionBox);
		content.add(checkBoxCentersSelectedNode);
		Box zoomBox = Box.createHorizontalBox();
		zoomBox.add(tglbtnChangeZoom);
		zoomBox.add(lblZoomFactor);
		content.add(zoomBox);
		content.add(checkBoxShowOnlySelectedNodes);
		content.add(checkBoxShowAncestors);
		content.add(checkBoxShowDescendants);
		content.add(tglbtnSetFilter);
		content.add(filterConditionComponentBox);

		Box contentWithMargins = Box.createHorizontalBox();
		TranslatedElementFactory.createTitledBorder(contentWithMargins, "slide.content");
		contentWithMargins.add(Box.createHorizontalGlue());
		contentWithMargins.add(content);
		contentWithMargins.add(Box.createHorizontalGlue());
		return contentWithMargins;
	}


	public void setSlide(Slide newSlide) {
		if(slide != null)
			slide.removeSlideChangeListener(slideChangeListener);
		this.slide = newSlide;
		if(newSlide != null){
			for(JComponent c : allButtons)
				c.setEnabled(true);
			updateUI();
			slide.addSlideChangeListener(slideChangeListener);
		}
		else{
			disableUI();
		}
	}


	private void disableUI() {
		for(JComponent c : allButtons)
			c.setEnabled(false);
		removeFilterComponent();
	}


	private void updateUI() {
		final boolean showsOnlySpecificNodes = slide.showsOnlySpecificNodes();
		checkBoxShowOnlySelectedNodes.setSelected(showsOnlySpecificNodes);
		final boolean centersSelectedNode = slide.centersSelectedNode();
		checkBoxCentersSelectedNode.setSelected(centersSelectedNode);
		final ASelectableCondition filterCondition = slide.getFilterCondition();
		for(JComponent c : filterRelatedButtons)
			c.setEnabled(showsOnlySpecificNodes || filterCondition != null);
		final boolean changesZoom = slide.changesZoom();
		tglbtnChangeZoom.setSelected(changesZoom);
		lblZoomFactor.setText(changesZoom ? Math.round(slide.getZoom() * 100) + "%" : "");
		checkBoxShowOnlySelectedNodes.setSelected(showsOnlySpecificNodes);
		checkBoxShowAncestors.setSelected(slide.showsAncestors());
		checkBoxShowDescendants.setSelected(slide.showsDescendants());
		checkBoxShowAncestors.setSelected(slide.showsAncestors());
		tglbtnSetFilter.setSelected(filterCondition != null);
		removeFilterComponent();
		if(filterCondition != null) {
			final JComponent component = filterCondition.createGraphicComponent();
			TranslatedElementFactory.createTitledBorder(component, "slide.filter");
			filterConditionComponentBox.add(component);
		}
		filterConditionComponentBox.revalidate();
	}

	public void removeFilterComponent() {
		while(filterConditionComponentBox.getComponentCount() > 0)
			filterConditionComponentBox.remove(0);
	}
	
	

}