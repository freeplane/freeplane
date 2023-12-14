/*
 * Created on 19 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.graph;

import java.util.AbstractMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

public class GraphCycleFinder<V>{

    private static final CycleSearchStopException CYCLE_SEARCH_STOP_EXCEPTION = new CycleSearchStopException();
    private enum StopEdgeNode{START, STOP}

    static private class CycleSearchStopException extends RuntimeException{
        private static final long serialVersionUID = 1L;
    }

    private final Graph<Object, DefaultEdge> graph;

    public GraphCycleFinder() {
        this.graph = new SimpleDirectedGraph<>(DefaultEdge.class);
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

    public Set<Map.Entry<V, V>> findSimpleCycles()
    {
        Set<Map.Entry<V, V>> result = new LinkedHashSet<>();
        JohnsonSimpleCycles<Object, DefaultEdge> cycleFinder = new JohnsonSimpleCycles<>(graph);
        try {
            cycleFinder.findSimpleCycles(cycle -> {
                if(cycle.get(0) == StopEdgeNode.START)
                    throw CYCLE_SEARCH_STOP_EXCEPTION;
                else {
                    @SuppressWarnings("unchecked")
                    List<V> typedCycle = (List<V>) cycle;
                    for(int n = 0; n < typedCycle.size(); n++) {
                        V origin = typedCycle.get(n);
                        V target = typedCycle.get((n+1) % cycle.size());
                        result.add(new AbstractMap.SimpleEntry<>(origin, target));
                    }
                }
            });
        } catch (CycleSearchStopException e) {/**/}
        return result;
    }


}
