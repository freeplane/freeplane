/*
 * Created on 21 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.graph;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jgrapht.Graph;
import org.jgrapht.traverse.DepthFirstIterator;

public class GraphExplorer {

    private static final class BothDirectionsIterator<V, E> extends DepthFirstIterator<V, E> {
        private BothDirectionsIterator(Graph<V, E> g, Iterable<V> startVertices) {
            super(g, startVertices);
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

    static <V, E> void exploreGraph(Graph<Object, E> graph,
            Iterable<V> startVertices,
            Function<V, Stream<V>> outgoingEdgesProvider,
            Function<V, Stream<V>> incomingEdgesProvider) {
        Class<?> vertexClass = startVertices.iterator().next().getClass();
        @SuppressWarnings("unchecked")
        DepthFirstIterator<Object, E> iterator = new BothDirectionsIterator<>(graph, (Iterable<Object>)startVertices);
        while (iterator.hasNext()) {
            Object currentElement = iterator.next();

            if(vertexClass.isInstance(currentElement)) {
                @SuppressWarnings("unchecked")
                Stream<V> outgoingElements = outgoingEdgesProvider.apply((V)currentElement);
                outgoingElements.forEach(outgoingElement -> {
                    graph.addVertex(outgoingElement);
                    graph.addEdge(currentElement, outgoingElement);
                });

                @SuppressWarnings("unchecked")
                Stream<V> incomingElements = incomingEdgesProvider.apply((V)currentElement);
                incomingElements.forEach(incomingElement -> {
                    graph.addVertex(incomingElement);
                    graph.addEdge(incomingElement, currentElement);
                });
            }
        }
    }
}
