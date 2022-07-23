package org.schism.collections

public inline fun <T> listBy(size: Int, crossinline getElement: (Int) -> T): List<T> {
    return object : AbstractList<T>() {
        override val size: Int get() {
            return size
        }

        override fun get(index: Int): T {
            return getElement(index)
        }
    }
}
