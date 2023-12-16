package di

interface DependencyProvider<T> {
    fun get(): T
}