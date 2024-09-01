/*
 * Created on 22 Mar 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.graph;

import org.jgrapht.graph.DefaultWeightedEdge;

public class CoupledWeightedEdge extends DefaultWeightedEdge{
    private static final long serialVersionUID = 1L;
    private double coupling = 0;

    void addCoupling(double addedCoupling) {
        coupling += addedCoupling;
    }

    double getCoupling() {
        return coupling;
    }

}
