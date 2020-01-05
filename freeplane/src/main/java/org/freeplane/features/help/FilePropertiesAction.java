/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.help;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.Filter;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.icon.factory.IconFactory;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * @author Stefan Ott
 */
class FilePropertiesAction extends AFreeplaneAction {
	private static final long serialVersionUID = 1L;

	FilePropertiesAction() {
		super("FilePropertiesAction");
	}

	/**
	 * Gets called when File -> Properties is selected
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		//variables for information to be displayed
		final String fileNamePath, fileSavedDateTime, fileSize;
		final int fileChangesSinceSave;
		//get information
		//if file has been saved once
		final MapModel map = Controller.getCurrentController().getMap();
        if (map.getFile() != null) {
			//fileNamePath
			fileNamePath = map.getFile().toString();
			//fleSavedDateTime as formatted string
			final Calendar c = Calendar.getInstance();
			c.setTimeInMillis(map.getFile().lastModified());
			final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
			fileSavedDateTime = df.format(c.getTime());
			//fileSize as formatted string
			final DecimalFormat def = new DecimalFormat();
			def.setGroupingUsed(true);
			fileSize = def.format(map.getFile().length()) + " "
			        + TextUtils.getText("FileRevisionsDialog.file_size");
			//fileChangesSinceSave
			fileChangesSinceSave = map.getNumberOfChangesSinceLastSave();
		}
		else {
			fileNamePath = TextUtils.getText("FileProperties_NeverSaved");
			fileSavedDateTime = TextUtils.getText("FileProperties_NeverSaved");
			fileSize = TextUtils.getText("FileProperties_NeverSaved");
			fileChangesSinceSave = 0;
		}
		//node statistics
		final NodeModel rootNode = map.getRootNode();
        final int nodeMainBranches = rootNode.getChildCount();
		final ICondition trueCondition = new ICondition() {
		    @Override
			public boolean checkNode(NodeModel node) {
		        return ! node.isHiddenSummary();
		    }
		};
        final ICondition isLeafCondition = new ICondition() {
            @Override
			public boolean checkNode(NodeModel node) {
                return node.isLeaf() && ! node.isHiddenSummary();
            }
        };
        final int nodeTotalNodeCount = getNodeCount(rootNode, trueCondition);
        final int nodeTotalLeafCount = getNodeCount(rootNode, isLeafCondition);
        final Filter filter = map.getFilter();
        final int nodeTotalFiltered;
        if(filter != null && filter.getCondition() != null){
            final ICondition matchesFilterCondition = new ICondition() {
                @Override
				public boolean checkNode(NodeModel node) {
                    return node.getFilterInfo().isMatched()  && ! node.isHiddenSummary();
                }
            };
            nodeTotalFiltered = getNodeCount(rootNode, matchesFilterCondition);
        }
        else{
            nodeTotalFiltered = -1;
        }
		//Multiple nodes may be selected
		final Set<NodeModel> nodes = Controller.getCurrentController().getSelection().getSelection();
        long nodeRelativeChildCount = nodes.stream().mapToLong(this::countChildren).sum();
        final Set<NodeModel> selectionRoots = getSelectionRoots(nodes);
        long nodeRelativeNodeCount = selectionRoots.stream()//
        		.mapToInt(n -> getNodeCount(n, trueCondition)).sum();
        long nodeRelativeLeafCount = selectionRoots.stream()//
        		.mapToInt(n -> getNodeCount(n, isLeafCondition)).sum();
		final int nodeSelectedNodeCount = Controller.getCurrentController().getSelection().getSelection().size();
		//build component
		final JPanel panel = new JPanel();
		final GridBagLayout gridbag = new GridBagLayout();
		panel.setLayout(gridbag);
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
		    BorderFactory.createEmptyBorder(5, 0, 5, 0)));
		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.ipady = 5;
		c.ipadx = 0;
		c.insets = new Insets(0, 10, 0, 10);
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		//fileNamePath
		final URL imageURL = ResourceController.getResourceController().getIconResource("/images/filenew.svg");
		final JLabel fileIcon = new JLabel(IconFactory.getInstance().getIcon(imageURL));
		gridbag.setConstraints(fileIcon, c);
		panel.add(fileIcon);
		c.gridx = 1;
		final JLabel fileNamePathText = new JLabel(TextUtils.getText("FileProperties_FileName"));
		gridbag.setConstraints(fileNamePathText, c);
		panel.add(fileNamePathText);
		c.gridx = 2;
		final JLabel fileNamePathLabel = new JLabel(fileNamePath);
		gridbag.setConstraints(fileNamePathLabel, c);
		panel.add(fileNamePathLabel);
		//fileSize
		c.gridy++;
		c.gridx = 1;
		final JLabel fileSizeText = new JLabel(TextUtils.getText("FileProperties_FileSize"));
		gridbag.setConstraints(fileSizeText, c);
		panel.add(fileSizeText);
		c.gridx = 2;
		final JLabel fileSizeLabel = new JLabel(fileSize);
		gridbag.setConstraints(fileSizeLabel, c);
		panel.add(fileSizeLabel);
		//fileSavedDateTime
		c.gridy++;
		c.gridx = 1;
		final JLabel fileSavedDateTimeText = new JLabel(TextUtils.getText("FileProperties_FileSaved"));
		gridbag.setConstraints(fileSavedDateTimeText, c);
		panel.add(fileSavedDateTimeText);
		c.gridx = 2;
		final JLabel fileSavedDateTimeLabel = new JLabel(fileSavedDateTime);
		gridbag.setConstraints(fileSavedDateTimeLabel, c);
		panel.add(fileSavedDateTimeLabel);
		//fileChangesSinceSave
		c.gridy++;
		c.gridx = 1;
		final JLabel fileChangesSinceSaveText = new JLabel(TextUtils.getText("FileProperties_ChangesSinceLastSave"));
		gridbag.setConstraints(fileChangesSinceSaveText, c);
		panel.add(fileChangesSinceSaveText);
		c.gridx = 2;
		final JLabel fileChangesSinceSaveLabel = new JLabel(String.valueOf(fileChangesSinceSave));
		gridbag.setConstraints(fileChangesSinceSaveLabel, c);
		panel.add(fileChangesSinceSaveLabel);
		//Separator
		c.gridy++;
		c.gridx = 0;
		c.insets = new Insets(5, 10, 5, 10);
		c.ipady = 2;
		c.gridwidth = 3;
		final JSeparator js = new JSeparator(SwingConstants.HORIZONTAL);
		js.setLayout(gridbag);
		js.setBorder(BorderFactory.createEtchedBorder());
		js.setPreferredSize(new Dimension(0, 0));
		c.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(js, c);
		panel.add(js);
		//nodeTotalNodeCount
		c.gridy++;
		c.insets = new Insets(0, 10, 0, 10);
		c.ipady = 5;
		c.gridwidth = 1;
		c.gridx = 0;
		final URL imageURL2 = ResourceController.getResourceController().getIconResource("/images/MapStats.svg");
		final JLabel MapStatsIcon = new JLabel(IconFactory.getInstance().getIcon(imageURL2));
		gridbag.setConstraints(MapStatsIcon, c);
		panel.add(MapStatsIcon);
		c.gridx = 1;
		final JLabel nodeTotalNodeCountText = new JLabel(TextUtils.getText("FileProperties_TotalNodeCount"));
		gridbag.setConstraints(nodeTotalNodeCountText, c);
		panel.add(nodeTotalNodeCountText);
		c.gridx = 2;
		final JLabel nodeTotalNodeCountLabel = new JLabel(String.valueOf(nodeTotalNodeCount));
		gridbag.setConstraints(nodeTotalNodeCountLabel, c);
		panel.add(nodeTotalNodeCountLabel);
		//nodeTotalFiltered
		if(nodeTotalFiltered != -1){
		    c.gridy++;
		    c.gridx = 1;
		    final JLabel nodeTotalFilteredLabelText = new JLabel(TextUtils.getText("FileProperties_TotalFilteredCount"));
		    gridbag.setConstraints(nodeTotalFilteredLabelText, c);
		    panel.add(nodeTotalFilteredLabelText);
		    c.gridx = 2;
		    final JLabel nodeTotalFilteredLabel = new JLabel(String.valueOf(nodeTotalFiltered));
		    gridbag.setConstraints(nodeTotalFilteredLabel, c);
		    panel.add(nodeTotalFilteredLabel);
		}
        //nodeTotalLeafCount
        c.gridy++;

        c.gridx = 1;
		final JLabel nodeTotalLeafCountText = new JLabel(TextUtils.getText("FileProperties_TotalLeafCount"));
		gridbag.setConstraints(nodeTotalLeafCountText, c);
		panel.add(nodeTotalLeafCountText);
		c.gridx = 2;
		final JLabel nodeTotalLeafCountLabel = new JLabel(String.valueOf(nodeTotalLeafCount));
		gridbag.setConstraints(nodeTotalLeafCountLabel, c);
		panel.add(nodeTotalLeafCountLabel);
		//nodeMainBranches
		c.gridy++;
		c.gridx = 1;
		final JLabel nodeMainBranchesText = new JLabel(TextUtils.getText("FileProperties_MainBranchCount"));
		gridbag.setConstraints(nodeMainBranchesText, c);
		panel.add(nodeMainBranchesText);
		c.gridx = 2;
		final JLabel nodeMainBranchesLabel = new JLabel(String.valueOf(nodeMainBranches));
		gridbag.setConstraints(nodeMainBranchesLabel, c);
		panel.add(nodeMainBranchesLabel);
		//Separator
		c.gridy++;
		c.gridx = 0;
		c.insets = new Insets(5, 10, 5, 10);
		c.ipady = 2;
		c.gridwidth = 3;
		final JSeparator js2 = new JSeparator(SwingConstants.HORIZONTAL);
		js2.setLayout(gridbag);
		js2.setBorder(BorderFactory.createEtchedBorder());
		js2.setPreferredSize(new Dimension(0, 0));
		c.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(js2, c);
		panel.add(js2);
		//nodeRelativeNodeCount
		c.gridy++;
		c.insets = new Insets(0, 10, 0, 10);
		c.ipady = 5;
		c.gridwidth = 1;
		c.gridx = 0;
		final URL imageURL3 = ResourceController.getResourceController().getIconResource("/images/BranchStats.svg");
		final JLabel BranchStatsIcon = new JLabel(IconFactory.getInstance().getIcon(imageURL3));
		gridbag.setConstraints(BranchStatsIcon, c);
		panel.add(BranchStatsIcon);
		c.gridx = 1;
		final JLabel nodeRelativeNodeCountText = new JLabel(TextUtils.getText("FileProperties_BranchNodeCount"));
		gridbag.setConstraints(nodeRelativeNodeCountText, c);
		panel.add(nodeRelativeNodeCountText);
		c.gridx = 2;
		final JLabel nodeRelativeNodeCountLabel = new JLabel(String.valueOf(nodeRelativeNodeCount));
		gridbag.setConstraints(nodeRelativeNodeCountLabel, c);
		panel.add(nodeRelativeNodeCountLabel);
		//nodeRelativeLeafCount
		c.gridy++;
		c.gridx = 1;
		final JLabel nodeRelativeLeafCountText = new JLabel(TextUtils.getText("FileProperties_BranchLeafCount"));
		gridbag.setConstraints(nodeRelativeLeafCountText, c);
		panel.add(nodeRelativeLeafCountText);
		c.gridx = 2;
		final JLabel nodeRelativeLeafCountLabel = new JLabel(String.valueOf(nodeRelativeLeafCount));
		gridbag.setConstraints(nodeRelativeLeafCountLabel, c);
		panel.add(nodeRelativeLeafCountLabel);
		//nodeRelativeChildCount
		c.gridy++;
		c.gridx = 1;
		final JLabel nodeRelativeChildCountText = new JLabel(TextUtils.getText("FileProperties_NodeChildCount"));
		gridbag.setConstraints(nodeRelativeChildCountText, c);
		panel.add(nodeRelativeChildCountText);
		c.gridx = 2;
		final JLabel nodeRelativeChildCountLabel = new JLabel(String.valueOf(nodeRelativeChildCount));
		gridbag.setConstraints(nodeRelativeChildCountLabel, c);
		panel.add(nodeRelativeChildCountLabel);
		//nodeSelectedNodeCount
		c.gridy++;
		c.gridx = 1;
		final JLabel nodeSelectedNodeCountText = new JLabel(TextUtils.getText("FileProperties_NodeSelectionCount"));
		gridbag.setConstraints(nodeSelectedNodeCountText, c);
		panel.add(nodeSelectedNodeCountText);
		c.gridx = 2;
		final JLabel nodeSelectedNodeCountLabel = new JLabel(String.valueOf(nodeSelectedNodeCount));
		gridbag.setConstraints(nodeSelectedNodeCountLabel, c);
		panel.add(nodeSelectedNodeCountLabel);
		//Show dialog
		JOptionPane.showMessageDialog(UITools.getCurrentRootComponent(), panel,
		    TextUtils.getText("FilePropertiesAction.text"), JOptionPane.PLAIN_MESSAGE);
	}

	private long countChildren(NodeModel n) {
		return n.getChildren().stream().filter(n2 -> ! n2.isHiddenSummary()).count();
	}

	private Set<NodeModel> getSelectionRoots(final Set<NodeModel> nodes) {
		final Set<NodeModel> selectionRoots = new HashSet<>();
		NODES: for (final NodeModel n : nodes) {
			for(NodeModel parent = n.getParentNode(); parent != null; parent = parent.getParentNode()) {
				if(nodes.contains(parent))
					continue NODES;
			}
			selectionRoots.add(n);
		}
		return selectionRoots;
	}

	/**
	 * Builds an array containing nodes form the given node on downwards.
	 *
	 * @param node The node from which on to search
	 * @param condition If true only leave nodes are included in the return list,
	 * otherwise all nodes from the selected on are included
	 *
	 * @return Returns a list of nodes
	 */
	private int getNodeCount(final NodeModel node, final ICondition condition) {
	    int result = 0;
		final Enumeration<NodeModel> children = node.children();
		if (condition.checkNode(node)) {
		    result++;
		}
		while (children.hasMoreElements()) {
			final NodeModel child = children.nextElement();
			result += getNodeCount(child, condition);
		}
		return result;
	}
}
