/*
 * Created on 21 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.main.codeexplorermode;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;

public class GraphExplorer {

    private static final class BothDirectionsIterator<V, E> extends DepthFirstIterator<V, E> {
        private BothDirectionsIterator(Graph<V, E> g) {
            super(g);
        }

        @Override
        protected Set<E> selectOutgoingEdges(V vertex) {
            Set<E> outgoingEdges = graph.outgoingEdgesOf(vertex);
            Set<E> incomingEdges = graph.incomingEdgesOf(vertex);
            return combine(outgoingEdges, incomingEdges);
        }

        private Set<E> combine(Set<E> outgoingEdges, Set<E> incomingEdges) {
            if(outgoingEdges.isEmpty())
                return incomingEdges;
            if(incomingEdges.isEmpty())
                return outgoingEdges;

            Set<E> combinedEdges = new HashSet<>(outgoingEdges);
            combinedEdges.addAll(incomingEdges);
            return combinedEdges;
        }
    }

    static public <V, E> void exploreGraph(V startVertex, Class<E> edgeClass, Function<V, Set<V>> outgoingEdgesProvider, Function<V, Set<V>> incomingEdgesProvider) {
        Graph<V, E> graph = new DefaultDirectedGraph<>(edgeClass);
        DepthFirstIterator<V, E> iterator = new BothDirectionsIterator<V, E>(graph);

        graph.addVertex(startVertex);

        while (iterator.hasNext()) {
            V currentElement = iterator.next();

            Set<V> outgoingElements = outgoingEdgesProvider.apply(currentElement);
            outgoingElements.forEach(outgoingElement -> {
                graph.addVertex(outgoingElement);
                graph.addEdge(currentElement, outgoingElement);
            });

            Set<V> incomingElements = incomingEdgesProvider.apply(currentElement);
            incomingElements.forEach(incomingElement -> {
                graph.addVertex(incomingElement);
                graph.addEdge(incomingElement, currentElement);
            });
        }

    }
}
