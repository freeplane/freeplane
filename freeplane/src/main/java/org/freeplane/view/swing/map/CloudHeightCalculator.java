package org.freeplane.view.swing.map;

import org.freeplane.features.cloud.CloudModel;
import org.freeplane.view.swing.map.cloud.CloudView;

public class CloudHeightCalculator {
	public static final CloudHeightCalculator INSTANCE = new CloudHeightCalculator();
    /**
     * Calculates the tree height increment because of the clouds.
     */
    public int getAdditionalCloudHeigth(final NodeView node) {
        if (!node.isSubtreeVisible()) {
            return 0;
        }
        final CloudModel cloud = node.getCloudModel();
        if (cloud != null) {
            return CloudView.getAdditionalHeigth(cloud, node);
        }
        else {
            return 0;
        }
    }

}
