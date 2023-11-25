/*
 * Created on 19 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class GraphCycleFinder<V>{

    private static final CycleSearchStopException CYCLE_SEARCH_STOP_EXCEPTION = new CycleSearchStopException();
    private enum StopEdgeNode{START, STOP}

    static private class CycleSearchStopException extends RuntimeException{
        private static final long serialVersionUID = 1L;
    }

    private final Graph<Object, DefaultEdge> graph;

    public GraphCycleFinder() {
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
    }

    public DefaultEdge addEdge(V source, V target) {
        return addEdgeObject(source, target);
    }

    DefaultEdge addEdgeObject(Object source, Object target) {
        graph.addVertex(source);
        graph.addVertex(target);
        return graph.addEdge(source, target);
    }

    public boolean addNode(V v) {
        return graph.addVertex(v);
    }

    public void stopSearchHere() {
        addEdgeObject(StopEdgeNode.START, StopEdgeNode.STOP);
        addEdgeObject(StopEdgeNode.STOP, StopEdgeNode.START);

    }

    public void exploreGraph(
            Iterable<V> startVertices,
            Function<V, Stream<V>> outgoingEdgesProvider,
            Function<V, Stream<V>> incomingEdgesProvider) {
        GraphExplorer.exploreGraph(graph, startVertices, outgoingEdgesProvider, incomingEdgesProvider);
    }

    public List<List<V>> findSimpleCycles()
    {
        List<List<V>> result = new ArrayList<>();
        JohnsonSimpleCycles<Object, DefaultEdge> cycleFinder = new JohnsonSimpleCycles<>(graph);
        try {
            cycleFinder.findSimpleCycles(cycle -> {
                if(cycle.get(0) == StopEdgeNode.START)
                    throw CYCLE_SEARCH_STOP_EXCEPTION;
                else {
                    @SuppressWarnings("unchecked")
                    List<V> typedCycle = (List<V>) cycle;
                    result.add(typedCycle);
                }
            });
        } catch (CycleSearchStopException e) {/**/}
        return result;
    }


}
