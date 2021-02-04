package com.tecacet.dependencies

import guru.nidi.graphviz.attribute.Shape
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.model.Factory
import guru.nidi.graphviz.model.Node
import org.apache.maven.model.Dependency
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileReader

/**
 * Traverse a directory structure and read all poms in memory
 * @param directory parent directory
 * @return a map of models indexed by name
 */
fun loadPoms(directory: String): Map<String, Model> {
    val reader = MavenXpp3Reader()
    return File(directory).walkTopDown().filter { it.name == "pom.xml" }
        .map { reader.read(FileReader(it)) }
        .map { Pair(it.artifactId, it) }.toMap()
}

/**
 * Build a JGrapht graph for the dependencies in a colletion of POMs
 * @param models : The POM models
 * @param predicate: A filter for the dependencies to consider
 */
fun buildPomGraph(
    models: Collection<Model>,
    predicate: (Dependency) -> Boolean
): DefaultDirectedGraph<String, DefaultEdge> {

    val graph = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)

    for (model in models) {
        val artifactId = model.artifactId
        graph.addVertex(artifactId)
        val dependencies = model.dependencies.filter(predicate)
        dependencies.forEach {
            graph.addVertex(it.artifactId)
            graph.addEdge(artifactId, it.artifactId)
        }
    }
    return graph

}

/**
 * Same as @see buildPomGraph, with a dependency filter on a specific maven group Id
 * @param models : The POM models
 * @param groupId: The maven groupId, eg "com.tecacet"
 *
 */
fun buildPomGraphForGroup(models: Collection<Model>, groupId: String): DefaultDirectedGraph<String, DefaultEdge> {
    return buildPomGraph(models) { dependency -> dependency.groupId == groupId }
}

/**
 * Build a dependency graph where the nodes are only the parent poms.
 */
fun buildParentGraph(
    models: Map<String, Model>,
    predicate: (Dependency) -> Boolean
): DefaultDirectedGraph<String, DefaultEdge> {
    val log = LoggerFactory.getLogger("")
    val parentGraph = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)
    for (model in models.values) {
        val parent = model.parent
        val parentId: String = if (parent == null) model.artifactId else model.parent.artifactId
        parentGraph.addVertex(parentId)
        val dependencies = model.dependencies.filter(predicate)
        for (dependency in dependencies) {
            val dependencyId = dependency.artifactId
            val dependencyModel = models[dependencyId]
            if (dependencyModel == null) {
                log.warn("Artifact ${dependencyId} is missing!!!")
                continue
            }
            val dependencyParent = dependencyModel.parent
            val dependencyParentId =
                if (dependencyParent == null) dependencyModel.artifactId else dependencyParent.artifactId
            parentGraph.addVertex(dependencyParentId)
            parentGraph.addEdge(parentId, dependencyParentId)
        }
    }
    return parentGraph
}

/**
 * Build a dependency graph where the nodes are only the parent poms.
 */
fun buildParentGraphForGroup(models: Map<String, Model>, groupId: String): DefaultDirectedGraph<String, DefaultEdge> {
    return buildParentGraph(models) { dependency -> dependency.groupId == groupId }
}

/**
 * Build a graph layout using graphviz and export it as a PDF
 */
fun <V, E> toGraphviz(g: org.jgrapht.Graph<V, E>, name: String): guru.nidi.graphviz.model.Graph {

    var gv = Factory.graph(name).directed()
        .nodeAttr().with(Shape.BOX)

    val nodes = arrayListOf<Node>()
    g.edgeSet().forEach {
        val source = g.getEdgeSource(it)
        val target = g.getEdgeTarget(it)
        nodes.add(
            Factory.node(source.toString()).link(Factory.to(Factory.node(target.toString())))
        )
    }
    gv = gv.with(nodes)
    Graphviz.fromGraph(gv).render(Format.PNG).toFile(File("$name.png"))
    return gv.with(nodes)

}

/**
 * Clean up the graph by removing any vertices that have no linked dependencies
 */
fun <V, E> removeIsolatedVertices(g: org.jgrapht.Graph<V, E>): org.jgrapht.Graph<V, E> {

    fun isIsolated(vertex: V) = g.inDegreeOf(vertex) == 0 && g.outDegreeOf(vertex) == 0

    val isolated = g.vertexSet().filter { isIsolated(it) }
    isolated.forEach { g.removeVertex(it) }
    return g

}
