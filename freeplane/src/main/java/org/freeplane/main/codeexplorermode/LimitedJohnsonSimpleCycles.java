/*
 * Created on 19 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.main.codeexplorermode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class LimitedJohnsonSimpleCycles<V>{

    private static final CycleSearchStopException CYCLE_SEARCH_STOP_EXCEPTION = new CycleSearchStopException();

    static private class CycleSearchStopException extends RuntimeException{
        private static final long serialVersionUID = 1L;
    }

    private final Graph<V, DefaultEdge> graph;
    private final V stopEdgeOrigin;
    private final V stopEdgeTarget;


    public LimitedJohnsonSimpleCycles(Supplier<V> separatorNodeSupplier) {
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        this.stopEdgeOrigin = separatorNodeSupplier.get();
        this.stopEdgeTarget = separatorNodeSupplier.get();
    }

    public DefaultEdge addEdge(V source, V target) {
        graph.addVertex(source);
        graph.addVertex(target);
        return graph.addEdge(source, target);
    }

    public boolean addNode(V v) {
        return graph.addVertex(v);
    }

    public void stopSearchHere() {
        addEdge(stopEdgeOrigin, stopEdgeTarget);
    }

    public List<List<V>> findSimpleCycles()
    {
        List<List<V>> result = new ArrayList<>();
        JohnsonSimpleCycles<V, DefaultEdge> cycleFinder = new JohnsonSimpleCycles<>(graph);
        try {
            cycleFinder.findSimpleCycles(cycle -> {
                if(cycle.get(0).equals(stopEdgeOrigin))
                    throw CYCLE_SEARCH_STOP_EXCEPTION;
                else
                    result.add(cycle);
            });
        } catch (CycleSearchStopException e) {/**/}
        return result;
    }


}
