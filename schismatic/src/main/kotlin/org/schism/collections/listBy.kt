package org.schism.collections

inline fun <E> listBy(size: Int, crossinline computeElement: (index: Int) -> E): List<E> {
    return object : AbstractList<E>() {
        override val size get() = size

        override fun get(index: Int) = computeElement(index)
    }
}
