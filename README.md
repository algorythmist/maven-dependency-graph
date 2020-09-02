## How to use

```kotlin

// Place all the organizations projects (that you are interested in) under a directory, 
// Load all the poms under this directory
val models = loadPoms(parentDirectory) //returns Map<String, Model>

//Build a dependency graph for the poms. Supply an organization groupId or a custom filter
val graph = buildPomGraphForGroup(models.values, groupId)

//OPTIONAL: Perform transitive reduction to reduce the complexity
TransitiveReduction.INSTANCE.reduce(graph)

//Create a PDF of the graph 
//NOTE: You must have graphviz installed for this to work
toGraphviz(graph, "dependencies")

//Alternativly, export to Pajek NET format for further manipulation
FileWriter("parents.net").use {
    exporter.exportGraph(parentGraph, it)
}

```

If your modules are have many submodules and the graph gets complicated,
you can create a graph of just the parents

```kotlin
 val parentGraph = buildParentGraphForGroup(models, groupId)

//optionally, remove any vertices with no dependencies
removeIsolatedVertices(parentGraph)
```