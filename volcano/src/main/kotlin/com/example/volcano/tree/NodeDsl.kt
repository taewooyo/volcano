package com.example.volcano.tree

@DslMarker
annotation class TreeDslMarker

@TreeDslMarker
interface NodeDsl<in T> {

    @TreeDslMarker
    fun node(value: T, nodeBuilder: NodeDsl<T>.() -> Unit = {})
}

@TreeDslMarker
private class NodeDslImpl<T>(private val node: Tree.Node<T>) : NodeDsl<T> {

    private val elements = mutableListOf<NodeDslImpl<T>>()

    override fun node(value: T, nodeBuilder: NodeDsl<T>.() -> Unit) {
        elements += NodeDslImpl(Tree.Node(value)).apply(nodeBuilder)
    }

    fun build(): Tree.Node<T> {
        elements.forEach { node.add(it.build()) }
        return node
    }
}

@TreeDslMarker
interface TreeDsl<in T> : NodeDsl<T>

@TreeDslMarker
private class TreeDslImpl<T>(root: T) : TreeDsl<T> {

    private val rootNode = NodeDslImpl(Tree.Node(root))

    override fun node(value: T, nodeBuilder: NodeDsl<T>.() -> Unit) {
        rootNode.node(value, nodeBuilder)
    }

    fun build(): Tree<T> = Tree(rootNode.build())
}

@TreeDslMarker
fun <T> tree(root: T, treeBuilder: TreeDsl<T>.() -> Unit): Tree<T> =
    TreeDslImpl(root)
        .apply(treeBuilder)
        .build()