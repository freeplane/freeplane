package org.freeplane.features.presentations.mindmapmode;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import java.awt.GridBagLayout;
import java.awt.FlowLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.JLabel;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import javax.swing.JComboBox;
import javax.swing.Box;
import javax.swing.JButton;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.border.TitledBorder;

public class PresentationEditorPanel extends JPanel {

	/**
	 * Create the panel.
	 */
	public PresentationEditorPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		Box presentations = Box.createVerticalBox();
		presentations.setBorder(new TitledBorder(null, "Presentations", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(presentations);
		JComboBox comboBoxPresentations = new JComboBox(new String[]{"first presentation", "second presentation"});
		presentations.add(comboBoxPresentations);
		comboBoxPresentations.setEditable(true);
		
		Box presentationButtons = Box.createHorizontalBox();
		presentations.add(presentationButtons);
		
		JButton btnAppendPresentation = new JButton("Append");
		presentationButtons.add(btnAppendPresentation);
		
		JButton btnDeletePresentation = new JButton("Delete");
		presentationButtons.add(btnDeletePresentation);
		
		JButton btnRenamePresentation = new JButton("Rename");
		presentationButtons.add(btnRenamePresentation);
		
		Box presentationOrderButtons = Box.createHorizontalBox();
		presentations.add(presentationOrderButtons);
		
		JButton btnPresentationUp = new JButton("Up");
		presentationOrderButtons.add(btnPresentationUp);
		
		JButton btnPresentationDown = new JButton("Down");
		presentationOrderButtons.add(btnPresentationDown);
		
		JButton btnMovePresentationOrder = new JButton("Move");
		presentationOrderButtons.add(btnMovePresentationOrder);
		
		Box slides = Box.createVerticalBox();
		slides.setBorder(new TitledBorder(null, "Slides", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(slides);
		
		final String[] stepItems = new String[]{"first slide", "second slide"};
		JComboBox comboBoxSlides = new JComboBox(stepItems);
		comboBoxSlides.setEditable(true);
		slides.add(comboBoxSlides);
		
		Box stepButtons = Box.createHorizontalBox();
		slides.add(stepButtons);
		
		JButton btnAppendSlide = new JButton("Append");
		stepButtons.add(btnAppendSlide);
		
		JButton btnDeleteSlide = new JButton("Delete");
		stepButtons.add(btnDeleteSlide);

		JButton btnRenameSlide = new JButton("Rename");
		stepButtons.add(btnRenameSlide);
		
		Box slideOrderButtons = Box.createHorizontalBox();
		slides.add(slideOrderButtons);
		
		JButton btnSlideUp = new JButton("Up");
		slideOrderButtons.add(btnSlideUp);
		
		JButton btnSlideDown = new JButton("Down");
		slideOrderButtons.add(btnSlideDown);
		
		JButton btnMoveSlide = new JButton("Move");
		slideOrderButtons.add(btnMoveSlide);
		
		Box content = Box.createVerticalBox();
		content.setBorder(new TitledBorder(null, "Slide content", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(content);
		
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
		
		Box navigation = Box.createVerticalBox();
		navigation.setBorder(new TitledBorder(null, "Show", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(navigation);
		
		Box navigationButtons = Box.createHorizontalBox();
		navigation.add(navigationButtons);
		
		JButton btnPrevious = new JButton("Previous");
		navigationButtons.add(btnPrevious);
		
		JToggleButton tglbtnCurrent = new JToggleButton("Current");
		navigationButtons.add(tglbtnCurrent);
		
		JButton btnNext = new JButton("Next");
		navigationButtons.add(btnNext);
	}
}
