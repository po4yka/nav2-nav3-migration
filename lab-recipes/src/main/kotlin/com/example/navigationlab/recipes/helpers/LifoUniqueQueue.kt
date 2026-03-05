package com.example.navigationlab.recipes.helpers

import java.util.concurrent.CopyOnWriteArraySet

/**
 * LIFO queue with unique elements. Adding an existing item bumps it to the tail.
 * Ported from screentransitionsample's LifoUniqueQueue.
 */
internal class LifoUniqueQueue<T>(
    defaultDestinations: Set<T> = emptySet(),
) {
    private val items = CopyOnWriteArraySet(defaultDestinations)

    fun add(item: T): Boolean {
        if (items.contains(item)) {
            items.remove(item)
        }
        return items.add(item)
    }

    fun remove(): T? = if (items.isEmpty()) null else items.last().also { items.remove(it) }

    fun element(): T? = if (items.isEmpty()) null else items.last()

    fun toSet(): Set<T> = items
}
