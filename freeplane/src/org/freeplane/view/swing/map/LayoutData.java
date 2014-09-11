package org.freeplane.view.swing.map;

class LayoutData{
    final int[] lx;
    final int[] ly;
    final boolean[] free;
    final boolean[] summary;
    int left;
    int childContentHeight;
    int top;
    boolean rightDataSet;
    boolean leftDataSet;
    public LayoutData(int childCount) {
        super();
        this.lx = new int[childCount];
        this.ly = new int[childCount];
        this.free = new boolean[childCount];
        this.summary = new boolean[childCount];
        this.left = 0;
        this.childContentHeight = 0;
        this.top = 0;
        rightDataSet = false;
        leftDataSet = false;
    }
}