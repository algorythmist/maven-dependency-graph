package com.tecacet.dependencies

import org.jgrapht.Graph
import org.jgrapht.nio.ExportException
import org.jgrapht.nio.GraphExporter
import java.io.IOException
import java.io.Writer

/**
 * Exporter to the Pajek NET format
 */
class PajekExporter<V, E>(val labelProvider: (V) -> String) : GraphExporter<V, E> {

    constructor() : this({ it.toString() })

    override fun exportGraph(graph: Graph<V, E>, writer: Writer) {
        val index: MutableMap<String, Int> = HashMap()
        val vertices = graph.vertexSet()
        val numberOfVertices = vertices.size
        try {
            writer.append(String.format("*Vertices %d\n", numberOfVertices))
            var i = 0
            for (vertex in vertices) {
                index[vertex.toString()] = ++i
                writer.append(String.format("%d %s\n", i, labelProvider(vertex)))
            }
            writer.append("*Arcs\n")
            for (edge in graph.edgeSet()) {
                val from = index[graph.getEdgeSource(edge).toString()]
                val to = index[graph.getEdgeTarget(edge).toString()]
                val weight = graph.getEdgeWeight(edge)
                writer.append(String.format("%d %d %f\n", from, to, weight))
            }
        } catch (ioe: IOException) {
            throw ExportException(ioe)
        }
    }

}