package example

import di.DependencyProvider

fun main() {
    val carProvider: DependencyProvider<Car> = Car_DependencyProvider(Engine(), Wheel())
    val car = carProvider.get()
    println(car)
}