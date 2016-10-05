package org.freeplane.features.presentations.mindmapmode;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import org.freeplane.features.presentations.CollectionModel;
import org.freeplane.features.presentations.PresentationModel;
import org.freeplane.features.presentations.CollectionChangeListener;
import org.freeplane.features.presentations.CollectionChangedEvent;
import org.freeplane.features.presentations.SlideModel;

@SuppressWarnings("serial")
public class PresentationEditorPanel extends JPanel {

	/**
	 * Create the panel.
	 */
	public PresentationEditorPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		final CollectionBoxController<PresentationModel> presentationPanelController = new CollectionBoxController<>("Presentations", "New presentation");
		add(presentationPanelController.getCollectionBox());
		final CollectionModel<PresentationModel> presentations = new CollectionModel<>(PresentationModel.class);
		presentationPanelController.setCollection(presentations);
		final CollectionBoxController<SlideModel> slidePanelController = new CollectionBoxController<SlideModel>("Slides", "New slide");
		presentations.addSelectionChangeListener(new CollectionChangeListener<PresentationModel>() {
			
			@Override
			public void onCollectionChange(CollectionChangedEvent<PresentationModel> event) {
				PresentationModel presentationModel = event.collection.getCurrentElement();
				slidePanelController.setCollection(presentationModel != null ? presentationModel.slides : null);
			}
		});
		
		add(slidePanelController.getCollectionBox());
		JComponent content = createSlideContentBox();
		add(content);
		Box navigation = createNavigationBox();
		add(navigation);
	}

	private Box createNavigationBox() {
		Box navigation = Box.createVerticalBox();
		navigation.setBorder(new TitledBorder(null, "Show", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		Box navigationButtons = Box.createHorizontalBox();
		navigation.add(navigationButtons);
		
		JButton btnPrevious = new JButton("Previous");
		navigationButtons.add(btnPrevious);
		
		JToggleButton tglbtnCurrent = new JToggleButton("Current");
		navigationButtons.add(tglbtnCurrent);
		
		JButton btnNext = new JButton("Next");
		navigationButtons.add(btnNext);
		return navigation;
	}

	private Box createSlideContentBox() {
		Box content = Box.createVerticalBox();
		content.setBorder(new TitledBorder(null, "Slide content", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		Box slideButtons = Box.createVerticalBox();
		content.add(slideButtons);
		
		JToggleButton tglbtnHighlightSlideContent = new JToggleButton("Highlight content");
		tglbtnHighlightSlideContent.setToolTipText("Highlight visible and selected nodes in the map");
		tglbtnHighlightSlideContent.setAlignmentX(Component.CENTER_ALIGNMENT);
		slideButtons.add(tglbtnHighlightSlideContent);
		
		JButton btnSetSelectedNode = new JButton("Set selection");
		btnSetSelectedNode.setAlignmentX(Component.CENTER_ALIGNMENT);
		slideButtons.add(btnSetSelectedNode);
		
		Box zoomBox = Box.createHorizontalBox();
		slideButtons.add(zoomBox);
		
		JToggleButton tglbtnSetZoom = new JToggleButton("Set zoom");
		zoomBox.add(tglbtnSetZoom);
		
		JLabel lblNewLabel = new JLabel("100 %");
		zoomBox.add(lblNewLabel);
		
		JCheckBox checkBoxOnlySpecificNodes = new JCheckBox("Show only specific nodes");
		checkBoxOnlySpecificNodes.setAlignmentX(Component.CENTER_ALIGNMENT);
		slideButtons.add(checkBoxOnlySpecificNodes);
		
		JCheckBox checkBoxShowAncestors = new JCheckBox("Show ancestors");
		checkBoxShowAncestors.setAlignmentX(Component.CENTER_ALIGNMENT);
		slideButtons.add(checkBoxShowAncestors);
		
		JCheckBox checkBoxShowDescendants = new JCheckBox("Show descendants");
		checkBoxShowDescendants.setAlignmentX(Component.CENTER_ALIGNMENT);
		slideButtons.add(checkBoxShowDescendants);
		
		Box horizontalBox = Box.createHorizontalBox();
		slideButtons.add(horizontalBox);
		
		JButton btnAddNodes = new JButton("Add nodes");
		horizontalBox.add(btnAddNodes);
		btnAddNodes.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		JButton btnRemoveNodes = new JButton("Remove nodes");
		horizontalBox.add(btnRemoveNodes);
		btnRemoveNodes.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		JToggleButton tglbtnSetFilter = new JToggleButton("Set filter");
		slideButtons.add(tglbtnSetFilter);
		tglbtnSetFilter.setAlignmentX(Component.CENTER_ALIGNMENT);
		return content;
	}
}
