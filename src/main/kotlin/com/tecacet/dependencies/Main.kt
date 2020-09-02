package com.tecacet.dependencies

import org.jgrapht.alg.TransitiveReduction
import org.jgrapht.graph.DefaultEdge
import org.slf4j.LoggerFactory
import java.io.FileWriter

val groupId = "com.tecacet"
val parentDirectory = "~/home/user/"

val exporter = PajekExporter<String, DefaultEdge>()

/**
 * Example
 */
fun main() {

    val logger = LoggerFactory.getLogger("")
    logger.info("Loading models")
    val models = loadPoms(parentDirectory)
    logger.info("Loaded ${models.size} models")
    logger.info("Building graph")
    val graph = buildPomGraphForGroup(models.values, groupId)
    logger.info("Graph has ${graph.vertexSet().size} vertices and ${graph.edgeSet().size} edges")
    logger.info("Transitive reduction")
    TransitiveReduction.INSTANCE.reduce(graph)
    logger.info("Graph has ${graph.vertexSet().size} vertices and ${graph.edgeSet().size} edges")
    logger.info("Building visualization")
    toGraphviz(graph, "dependencies")

    val parentGraph = buildParentGraphForGroup(models, groupId)
    removeIsolatedVertices(parentGraph)
    logger.info("Graph has ${parentGraph.vertexSet().size} vertices and ${parentGraph.edgeSet().size} edges")

    toGraphviz(parentGraph, "parents")

    FileWriter("parents.net").use {
        exporter.exportGraph(parentGraph, it)
    }

}