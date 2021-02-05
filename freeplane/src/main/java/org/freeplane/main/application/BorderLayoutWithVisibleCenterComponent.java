package org.freeplane.main.application;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

class BorderLayoutWithVisibleCenterComponent extends BorderLayout {

    private static final long serialVersionUID = 8414055824538038007L;

    @Override
    public void layoutContainer(Container target) {
        super.layoutContainer(target);
        Component center = getLayoutComponent(CENTER);
        if(center == null || ! center.isVisible())
            return;
        int centerHeight = center.getHeight();
        int centerWidth = center.getWidth();
        int minimumCenterHeight = target.getHeight() / 10;
        int minimumCenterWidth = target.getWidth() / 10;
        if(centerHeight < minimumCenterHeight) {
            int maximumEdgeHeight = minimumCenterHeight * 9;
            Component north = getLayoutComponent(NORTH);
            int northHeight = north != null ? north.getHeight() : 0;
            Component south = getLayoutComponent(SOUTH);
            int southHeight = south != null ? south.getHeight() : 0;
            if(northHeight > 0 || southHeight > 0) {
                int newNorthHeight = maximumEdgeHeight * northHeight / (northHeight + southHeight);
                int northX = north != null ? north.getX() : center.getX();
                int northY = north != null ? north.getY() : center.getY();
                if(north != null)
                    north.setBounds(northX, northY, north.getWidth(), newNorthHeight);
                center.setBounds(center.getX(), northY + newNorthHeight, center.getWidth(), minimumCenterHeight);
                if(south != null) {
                    int newSouthHeight = maximumEdgeHeight * southHeight / (northHeight + southHeight);
                    south.setBounds(south.getX(), northY + newNorthHeight + minimumCenterHeight, south.getWidth(), newSouthHeight);
                }
            }
        }
        if (centerWidth <  minimumCenterWidth) {
            int maximumEdgeWidth = minimumCenterWidth * 9;
            Component west = getLayoutComponent(WEST);
            int westWidth = west != null ? west.getWidth() : 0;
            Component east = getLayoutComponent(EAST);
            int eastWidth = east != null ? east.getWidth() : 0;
            if(westWidth > 0 || eastWidth > 0) {
                int newWestWidth = maximumEdgeWidth * westWidth / (westWidth + eastWidth);
                int westX = west != null ? west.getX() : center.getX();
                int westY = west != null ? west.getY() : center.getY();
                if(west != null)
                    west.setBounds(westX, westY, newWestWidth, west.getHeight());
                center.setBounds(westX + newWestWidth, center.getY(), minimumCenterWidth, center.getHeight());
                if(east != null) {
                    int newEastWidth = maximumEdgeWidth * eastWidth / (westWidth + eastWidth);
                    east.setBounds(westX + newWestWidth + minimumCenterWidth, east.getY(),  newEastWidth, east.getHeight());
                }
            }
        }
    }

}